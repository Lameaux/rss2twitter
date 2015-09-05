package com.euromoby.r2t.core.rss.task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

import com.euromoby.r2t.core.Config;
import com.euromoby.r2t.core.http.HttpClientProvider;
import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.TwitterProvider;
import com.euromoby.r2t.core.twitter.model.TwitterAccount;
import com.euromoby.r2t.core.twitter.model.TwitterFriend;
import com.euromoby.r2t.core.twitter.model.TwitterRssFeed;
import com.euromoby.r2t.core.utils.StringUtils;

@Component
public class FeedBroadcastTask {

	private static final Logger log = LoggerFactory.getLogger(FeedBroadcastTask.class);

	public static final int HOUR_MICRO = 3600000;
	private static final int FOLLOWERS_COUNT = 200;

	private Config config;
	private TwitterManager twitterManager;
	private TwitterProvider twitterProvider;
	private HttpClientProvider httpClientProvider;

	private ExecutorService executor;

	@Autowired
	public FeedBroadcastTask(Config config, TwitterManager twitterManager, TwitterProvider twitterProvider, HttpClientProvider httpClientProvider) {
		this.config = config;
		this.twitterManager = twitterManager;
		this.twitterProvider = twitterProvider;
		this.httpClientProvider = httpClientProvider;
	}

	@Scheduled(fixedDelayString = "${twitter.task.delay}")
	public void execute() {

		executor = Executors.newFixedThreadPool(this.config.getTaskPoolSize());

		List<TwitterRssFeed> twitterRssFeeds = twitterManager.findRssFeeds();
		for (TwitterRssFeed twitterRssFeed : twitterRssFeeds) {
			long nextUpdate = twitterRssFeed.getUpdated() + twitterRssFeed.getFrequency() * HOUR_MICRO;
			if (nextUpdate > System.currentTimeMillis()) {
				continue;
			}
			FeedBroadcastWorker worker = new FeedBroadcastWorker(config, twitterManager, twitterProvider, httpClientProvider, twitterRssFeed);
			executor.submit(worker);
		}

		executor.shutdown();
		try {
			executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}

		List<TwitterAccount> accounts = twitterManager.findAccounts();
		long lastFollow = System.currentTimeMillis() - 15 * 60 * 1000;
		for (TwitterAccount twitterAccount : accounts) {
			if (twitterAccount.getLastFollow() < lastFollow) {
				try {
					// follow rsstw.it
					twitterProvider.follow(twitterAccount, config.getFollow());
					// follow suggested
					if (!StringUtils.nullOrEmpty(twitterAccount.getFollowScreenName())) {
						long cursor = -1;
						PagableResponseList<User> followers;
						boolean followed = false;
						int requestLimit = 7;
						while (!followed && cursor != 0 && requestLimit-- > 0) {
							followers = twitterProvider.getFollowersForScreenName(twitterAccount, twitterAccount.getFollowScreenName(), cursor, FOLLOWERS_COUNT);
							for (User user : followers) {
								String followerScreenName = user.getScreenName().toLowerCase();
								if (!twitterManager.hasFriend(twitterAccount.getScreenName(), followerScreenName)) {
									twitterProvider.follow(twitterAccount, followerScreenName);
									twitterManager.saveFriend(new TwitterFriend(twitterAccount.getScreenName(), followerScreenName));
									followed = true;
									break;
								}
							}
							cursor = followers.getNextCursor();
						}						
					}
				} catch (TwitterException te) {
					log.warn("Following failed for " + twitterAccount.getScreenName(), te);
				}
				twitterAccount.setLastFollow(System.currentTimeMillis());
				twitterManager.updateAccount(twitterAccount);
			}
		}

	}

}
