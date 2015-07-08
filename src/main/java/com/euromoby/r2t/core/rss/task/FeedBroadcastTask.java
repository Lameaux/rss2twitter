package com.euromoby.r2t.core.rss.task;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import twitter4j.Status;
import twitter4j.TwitterException;

import com.euromoby.r2t.core.Config;
import com.euromoby.r2t.core.http.HttpClientProvider;
import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.TwitterProvider;
import com.euromoby.r2t.core.twitter.model.TwitterAccount;
import com.euromoby.r2t.core.twitter.model.TwitterRssFeed;
import com.euromoby.r2t.core.twitter.model.TwitterStatusLog;
import com.euromoby.r2t.core.utils.StringUtils;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

@Component
public class FeedBroadcastTask {

	private static final Logger log = LoggerFactory.getLogger(FeedBroadcastTask.class);

	public static final int TWITTER_LIMIT = 140;
	public static final int HOUR_MICRO = 3600000;
	private static final int URL_ID_LENGTH=5;
	
	@Autowired
	private Config config;
	@Autowired
	private TwitterManager twitterManager;
	@Autowired
	private TwitterProvider twitterProvider;
	@Autowired
	private HttpClientProvider httpClientProvider;

	private void updateErrorStatus(TwitterRssFeed twitterRssFeed, String errorText) {
		twitterRssFeed.setStatus(TwitterRssFeed.STATUS_ERROR);
		twitterRssFeed.setErrorText(errorText);		
		twitterRssFeed.setUpdated(System.currentTimeMillis());
		twitterManager.updateRssFeed(twitterRssFeed);			
	}

	private void updateOkStatus(TwitterRssFeed twitterRssFeed) {
		twitterRssFeed.setStatus(TwitterRssFeed.STATUS_OK);
		twitterRssFeed.setErrorText(null);
		twitterRssFeed.setUpdated(System.currentTimeMillis());
		twitterManager.updateRssFeed(twitterRssFeed);			
	}	
	
	@Scheduled(fixedDelay = 5000) // 3600000 // = 1 hour
	public void execute() {
		
		int linkLength = config.getShortLinkPrefix().length() + URL_ID_LENGTH;
		
		List<TwitterRssFeed> twitterRssFeeds = twitterManager.findRssFeeds();
		for (TwitterRssFeed twitterRssFeed : twitterRssFeeds) {

			long nextUpdate = twitterRssFeed.getUpdated() + twitterRssFeed.getFrequency() * HOUR_MICRO;
			if (nextUpdate > System.currentTimeMillis()) {
				continue;
			}

			SyndFeed syndFeed = null;
			try {
				String rssContent = loadUrl(twitterRssFeed.getUrl());
				SyndFeedInput input = new SyndFeedInput();
				InputStream inputStream = IOUtils.toInputStream(rssContent, "UTF-8");
				syndFeed = input.build(new XmlReader(inputStream));				
			} catch (IOException io) {
				String message = "URL is unavailable";
				log.error(message + " {}", twitterRssFeed.getUrl());
				updateErrorStatus(twitterRssFeed, message);
				continue;
			} catch (Exception e) {
				String message = "Invalid format";
				log.error(message + " {}", twitterRssFeed.getUrl());
				updateErrorStatus(twitterRssFeed, message);
				continue;
			}

			if (syndFeed == null) {
				String message = "Invalid format";
				log.error(message + " {}", twitterRssFeed.getUrl());
				updateErrorStatus(twitterRssFeed, message);
				continue;
			}			

			List<SyndEntry> feedMessages = syndFeed.getEntries();
			if (feedMessages.isEmpty()) {
				updateOkStatus(twitterRssFeed);
				continue;
			}

			TwitterAccount twitterAccount = twitterManager.getAccountByScreenName(twitterRssFeed.getScreenName());
			
			for (SyndEntry feedMessage : feedMessages) {
				if (StringUtils.nullOrEmpty(feedMessage.getLink()) || StringUtils.nullOrEmpty(feedMessage.getTitle())) {
					continue;
				}
				if (twitterManager.alreadySent(twitterRssFeed.getScreenName(), feedMessage.getLink())) {
					continue;
				}				
				
				String statusText = createTweetText(feedMessage, linkLength);
				TwitterStatusLog twitterStatusLog = saveNewStatusLog(twitterAccount.getScreenName(), feedMessage.getLink(), statusText);
				
				try {
					
					Status status = twitterProvider.status(twitterAccount, statusText + " " + generateShortLink(twitterStatusLog.getId()));
					log.debug("{} updated status {}", twitterAccount.getScreenName(), status.getId());
					twitterStatusLog.setStatus(TwitterStatusLog.STATUS_OK);
				} catch (TwitterException e) {
					twitterStatusLog.setStatus(TwitterStatusLog.STATUS_ERROR);
					twitterStatusLog.setErrorText(e.getMessage());
				}
				updateStatusLog(twitterStatusLog);
				
				// break after first message
				break;
			}
			updateOkStatus(twitterRssFeed);
		}
	}

	private String generateShortLink(int id) {
		return config.getShortLinkPrefix() + "0" + Integer.toString(id, Character.MAX_RADIX);
	}
	
	private TwitterStatusLog saveNewStatusLog(String screenName, String url, String messageText) {
		TwitterStatusLog twitterStatusLog = new TwitterStatusLog();
		twitterStatusLog.setScreenName(screenName);
		twitterStatusLog.setUrl(url);
		twitterStatusLog.setMessage(messageText);
		twitterStatusLog.setUpdated(System.currentTimeMillis());
		twitterStatusLog.setStatus(TwitterStatusLog.STATUS_NEW);			
		twitterManager.saveStatusLog(twitterStatusLog);	
		return twitterStatusLog;
	}

	private void updateStatusLog(TwitterStatusLog twitterStatusLog) {
		twitterStatusLog.setUpdated(System.currentTimeMillis());
		twitterManager.updateStatusLog(twitterStatusLog);	
	}	
	
	private String loadUrl(String url) throws IOException {

		HttpGet request = new HttpGet(url);
		RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder();
		request.setConfig(requestConfigBuilder.build());
		CloseableHttpResponse response = httpClientProvider.executeRequest(request);
		try {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				EntityUtils.consumeQuietly(response.getEntity());
				throw new IOException(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity);
			EntityUtils.consumeQuietly(entity);
			return content;
		} finally {
			response.close();
		}
	}

	private String createTweetText(SyndEntry feedMessage, int urlLength) {
		String title = StringUtils.trimIfNotEmpty(feedMessage.getTitle());
		return limitLength(title, TWITTER_LIMIT - (urlLength + 1 /* space */));
	}

	private String limitLength(String s, int limit) {
		if (s.length() <= limit) {
			return s;
		}
		return s.substring(0, limit - 1);
	}

}
