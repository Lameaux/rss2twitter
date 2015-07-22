package com.euromoby.r2t.core.rss.task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.euromoby.r2t.core.Config;
import com.euromoby.r2t.core.http.HttpClientProvider;
import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.TwitterProvider;
import com.euromoby.r2t.core.twitter.model.TwitterRssFeed;

@Component
public class FeedBroadcastTask {

	public static final int HOUR_MICRO = 3600000;

	
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
		
	}

}
