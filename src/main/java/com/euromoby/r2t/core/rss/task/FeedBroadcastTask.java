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
				
				String statusText = createTweetText(feedMessage);
				
				TwitterStatusLog twitterStatusLog = new TwitterStatusLog();
				twitterStatusLog.setScreenName(twitterAccount.getScreenName());
				twitterStatusLog.setUrl(feedMessage.getLink());
				twitterStatusLog.setMessage(statusText);
				twitterStatusLog.setUpdated(System.currentTimeMillis());
				twitterStatusLog.setStatus(TwitterStatusLog.STATUS_OK);	
				
				try {
					Status status = twitterProvider.status(twitterAccount, statusText);
					log.debug("{} updated status {}", twitterAccount.getScreenName(), status.getId());
				} catch (TwitterException e) {
					twitterStatusLog.setStatus(TwitterStatusLog.STATUS_ERROR);
					twitterStatusLog.setErrorText(e.getMessage());
				}
				twitterManager.saveStatusLog(twitterStatusLog);					
				// break after first message
				break;
			}
			updateOkStatus(twitterRssFeed);
		}
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

	private String createTweetText(SyndEntry feedMessage) {
		String url = StringUtils.trimIfNotEmpty(feedMessage.getLink());
		String title = StringUtils.trimIfNotEmpty(feedMessage.getTitle());
		if (url.length() > TWITTER_LIMIT) {
			return limitLength(title, TWITTER_LIMIT);
		}
		int titleLimit = TWITTER_LIMIT - 1 /* space */- url.length();
		return limitLength(title, titleLimit) + " " + url;
	}

	private String limitLength(String s, int limit) {
		if (s.length() <= limit) {
			return s;
		}
		return s.substring(0, limit - 1);
	}

}
