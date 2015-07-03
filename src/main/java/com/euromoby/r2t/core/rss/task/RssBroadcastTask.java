package com.euromoby.r2t.core.rss.task;

import java.util.Date;
import java.util.List;

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
import org.springframework.stereotype.Component;

import twitter4j.Status;
import twitter4j.TwitterException;

import com.euromoby.r2t.core.http.HttpClientProvider;
import com.euromoby.r2t.core.rss.RSSFeedParser;
import com.euromoby.r2t.core.rss.model.Feed;
import com.euromoby.r2t.core.rss.model.FeedMessage;
import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.TwitterProvider;
import com.euromoby.r2t.core.twitter.model.TwitterAccount;
import com.euromoby.r2t.core.twitter.model.TwitterRssFeed;
import com.euromoby.r2t.core.twitter.model.TwitterStatusLog;
import com.euromoby.r2t.core.utils.StringUtils;

@Component
public class RssBroadcastTask {

	private static final Logger log = LoggerFactory.getLogger(RssBroadcastTask.class);

	public static final int TWITTER_LIMIT = 140;

	@Autowired
	private TwitterManager twitterManager;
	@Autowired
	private TwitterProvider twitterProvider;
	@Autowired
	private HttpClientProvider httpClientProvider;

	// @Scheduled(fixedDelay = 5000) // 600000
	public void execute() {
		// TODO check last updated
		List<TwitterRssFeed> rssFeeds = twitterManager.findRssFeeds();
		for (TwitterRssFeed rssFeed : rssFeeds) {

			try {
				String rssContent = loadUrl(rssFeed.getUrl());

				RSSFeedParser rssParser = new RSSFeedParser();
				Feed feed = rssParser.readFeed(rssContent);
				List<FeedMessage> feedMessages = feed.getMessages();
				if (feedMessages.isEmpty()) {
					continue;
				}

				// get last message
				FeedMessage feedMessage = feedMessages.get(0);
				if (StringUtils.nullOrEmpty(feedMessage.getLink()) || StringUtils.nullOrEmpty(feedMessage.getTitle())) {
					continue;
				}

				if (twitterManager.alreadySent(rssFeed.getScreenName(), feedMessage.getLink())) {
					continue;
				}

				TwitterAccount twitterAccount = twitterManager.getAccountByScreenName(rssFeed.getScreenName());
				String statusText = createTweetText(feedMessage);

				TwitterStatusLog twitterStatusLog = new TwitterStatusLog();
				twitterStatusLog.setScreenName(twitterAccount.getScreenName());
				twitterStatusLog.setUrl(feedMessage.getLink());
				twitterStatusLog.setMessage(statusText);
				twitterStatusLog.setUpdated(new Date());
				twitterStatusLog.setStatus(TwitterStatusLog.STATUS_OK);

				try {
					Status status = twitterProvider.status(twitterAccount, statusText);
					log.debug("{} updated status {}", twitterAccount.getScreenName(), status.getId());
				} catch (TwitterException e) {
					twitterStatusLog.setStatus(TwitterStatusLog.STATUS_ERROR);
					twitterStatusLog.setErrorText(e.getErrorMessage());
				}

				twitterManager.saveStatusLog(twitterStatusLog);

			} catch (Exception e) {
				log.error("Error processing RSS " + rssFeed.getUrl(), e);
				continue;
			}

		}
	}

	private String loadUrl(String url) throws Exception {

		HttpGet request = new HttpGet(url);
		RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder();
		request.setConfig(requestConfigBuilder.build());
		CloseableHttpResponse response = httpClientProvider.executeRequest(request);
		try {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				EntityUtils.consumeQuietly(response.getEntity());
				throw new Exception(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity);
			EntityUtils.consumeQuietly(entity);
			return content;
		} finally {
			response.close();
		}
	}

	private String createTweetText(FeedMessage feedMessage) {
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
