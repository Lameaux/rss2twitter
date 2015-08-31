package com.euromoby.r2t.core.rss.task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class FeedBroadcastWorker implements Callable<TwitterRssFeed> {

	private static final Logger log = LoggerFactory.getLogger(FeedBroadcastWorker.class);

	private static final int TWITTER_LIMIT = 116;
	private static final int TWITTER_PIC_LENGTH = 24;
	
	private static final Pattern IMAGE_PATTERN = Pattern.compile("<img[^>]+src=\"([^\">]+)\"",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static final int MIN_IMG_SIZE = 2 * 1024;
	
	private Config config;
	private TwitterManager twitterManager;
	private TwitterProvider twitterProvider;
	private HttpClientProvider httpClientProvider;
	private TwitterRssFeed twitterRssFeed;

	public FeedBroadcastWorker(Config config, TwitterManager twitterManager, TwitterProvider twitterProvider, HttpClientProvider httpClientProvider,
			TwitterRssFeed twitterRssFeed) {
		this.config = config;
		this.twitterManager = twitterManager;
		this.twitterProvider = twitterProvider;
		this.httpClientProvider = httpClientProvider;
		this.twitterRssFeed = twitterRssFeed;
	}

	@Override
	public TwitterRssFeed call() throws Exception {

		TwitterAccount twitterAccount = twitterManager.getAccountByScreenName(twitterRssFeed.getScreenName());
		if (twitterAccount == null) {
			String message = "Account not found";
			log.error(message + " {}", twitterRssFeed.getScreenName());
			updateErrorStatus(twitterRssFeed, message);
			return twitterRssFeed;
		}

		SyndFeed syndFeed = null;
		try {
			byte[] rssContent = loadUrl(twitterRssFeed.getUrl());
			SyndFeedInput input = new SyndFeedInput();
			String encoding = detectEncoding(rssContent);
			// read bytes with encoding
			String javaString = new String(rssContent, encoding);
			// stream as utf-8
			InputStreamReader isr = new InputStreamReader(new CharSequenceInputStream(javaString, "utf-8"));
			syndFeed = input.build(isr);
		} catch (IOException io) {
			String message = "URL is unavailable";
			log.error(message + " " + twitterRssFeed.getUrl(), io);
			updateErrorStatus(twitterRssFeed, message);
			return twitterRssFeed;
		} catch (Exception e) {
			String message = "Invalid format";
			log.error(message + " " + twitterRssFeed.getUrl(), e);
			updateErrorStatus(twitterRssFeed, message);
			return twitterRssFeed;
		}

		if (syndFeed == null) {
			String message = "Invalid format";
			log.error(message + " {}", twitterRssFeed.getUrl());
			updateErrorStatus(twitterRssFeed, message);
			return twitterRssFeed;
		}

		List<SyndEntry> feedMessages = syndFeed.getEntries();
		if (feedMessages.isEmpty()) {
			updateOkStatus(twitterRssFeed);
			return twitterRssFeed;
		}

		for (SyndEntry feedMessage : feedMessages) {
			if (StringUtils.nullOrEmpty(feedMessage.getLink()) || StringUtils.nullOrEmpty(feedMessage.getTitle())) {
				continue;
			}
			if (twitterManager.alreadySent(twitterRssFeed.getScreenName(), feedMessage.getLink())) {
				continue;
			}

			InputStream picture = createTweetPicture(feedMessage);
			boolean hasPicture = picture != null;
			String statusText = createTweetText(feedMessage, hasPicture);
			TwitterStatusLog twitterStatusLog = createStatusLog(twitterAccount.getScreenName(), feedMessage.getLink(), statusText);

			String shortLink = twitterManager.generateShortLink(twitterStatusLog.getId());
			try {
				
				String statusTextWithLink;
				if (statusText.contains(" #")) {
					statusTextWithLink = statusText.replaceFirst(" #", " " + shortLink + " #");
				} else {
					statusTextWithLink = statusText + " " + shortLink;
				}
				
				Status status = twitterProvider.status(twitterAccount, statusTextWithLink, picture);
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

		return twitterRssFeed;
	}

	private String detectEncoding(byte[] xmlContent) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(xmlContent);
		XmlReader xmlReader = new XmlReader(bais);
		try {
			if (xmlReader.getEncoding() != null) {
				return xmlReader.getEncoding().toLowerCase();
			}
			return null;
		} finally {
			xmlReader.close();	
		}
	}
	
	private TwitterStatusLog createStatusLog(String screenName, String url, String messageText) {
		
		TwitterStatusLog twitterStatusLog = twitterManager.findErrorStatus(screenName, url);
		if (twitterStatusLog == null) {
			twitterStatusLog = new TwitterStatusLog();	
		}
		twitterStatusLog.setScreenName(screenName);
		twitterStatusLog.setUrl(url);
		twitterStatusLog.setMessage(messageText);
		twitterStatusLog.setUpdated(System.currentTimeMillis());
		twitterStatusLog.setStatus(TwitterStatusLog.STATUS_NEW);
		twitterManager.saveOrUpdateStatusLog(twitterStatusLog);
		return twitterStatusLog;
	}

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

	private void updateStatusLog(TwitterStatusLog twitterStatusLog) {
		twitterStatusLog.setUpdated(System.currentTimeMillis());
		twitterManager.updateStatusLog(twitterStatusLog);
	}

	private byte[] loadUrl(String url) throws IOException {

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
			byte[] content = EntityUtils.toByteArray(entity);
			EntityUtils.consumeQuietly(entity);
			return content;
		} finally {
			response.close();
		}
	}

	private String createTweetText(SyndEntry feedMessage, boolean hasImage) {
		String title = StringUtils.trimIfNotEmpty(feedMessage.getTitle());
		
		List<SyndCategory> categories = feedMessage.getCategories();
		if (categories != null && !categories.isEmpty()) {
			for (SyndCategory category : categories) {
				String categoryName = category.getName();
				if (!StringUtils.nullOrEmpty(categoryName) && categoryName.length() > 2) {
					categoryName = categoryName.trim();
					categoryName = categoryName.replace("-", "_");
					categoryName = categoryName.replace(" ", " #");
					title = title + " #" + categoryName;
				}
			}
		}

		return limitLength(title, TWITTER_LIMIT - (hasImage ? TWITTER_PIC_LENGTH : 0));
	}

	private InputStream createTweetPicture(SyndEntry feedMessage) {
		if (feedMessage.getDescription() == null || feedMessage.getDescription().getValue() == null) {
			return null;
		}
		String description = feedMessage.getDescription().getValue();
		Matcher m = IMAGE_PATTERN.matcher(description);
		if (m.find()) {
			String pictureUrl = m.group(1);
			try {
				byte[] picture = loadUrl(pictureUrl);
				if (picture.length > MIN_IMG_SIZE) {
					return new ByteArrayInputStream(picture);					
				}
			} catch (IOException ioe) {
				log.debug("Unable to download {}", pictureUrl, ioe);
			}
		}
		return null;
	}
	
	private String limitLength(String s, int limit) {
		if (s.length() <= limit) {
			return s;
		}
		return s.substring(0, limit - 1);
	}



}
