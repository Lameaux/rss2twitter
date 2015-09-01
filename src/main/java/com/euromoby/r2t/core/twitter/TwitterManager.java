package com.euromoby.r2t.core.twitter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import twitter4j.auth.AccessToken;

import com.euromoby.r2t.core.Config;
import com.euromoby.r2t.core.twitter.dao.TwitterAccountDao;
import com.euromoby.r2t.core.twitter.dao.TwitterFriendDao;
import com.euromoby.r2t.core.twitter.dao.TwitterRssFeedDao;
import com.euromoby.r2t.core.twitter.dao.TwitterStatusLogDao;
import com.euromoby.r2t.core.twitter.model.TwitterAccount;
import com.euromoby.r2t.core.twitter.model.TwitterFriend;
import com.euromoby.r2t.core.twitter.model.TwitterRssFeed;
import com.euromoby.r2t.core.twitter.model.TwitterStatusLog;

@Component
public class TwitterManager {

	@Autowired
	private TwitterAccountDao twitterAccountDao;
	@Autowired
	private TwitterRssFeedDao twitterRssFeedDao;
	@Autowired
	private TwitterStatusLogDao twitterStatusLogDao;
	@Autowired
	private TwitterFriendDao twitterFriendDao;
	@Autowired
	private Config config;

	@Transactional(readOnly = true)
	public TwitterAccount getAccountByScreenName(String screenName) {
		return twitterAccountDao.findByScreenName(screenName);
	}

	@Transactional(readOnly = true)
	public boolean hasFriend(String screenName, String friendScreenName) {
		return twitterFriendDao.find(screenName, friendScreenName) != null;
	}	

	@Transactional
	public void saveFriend(TwitterFriend twitterFriend) {
		twitterFriendDao.save(twitterFriend);
	}	
	
	@Transactional(readOnly = true)
	public List<TwitterAccount> findAccounts() {
		return twitterAccountDao.findAll();
	}	
	
	@Transactional(readOnly = true)
	public List<TwitterRssFeed> findRssFeedsByScreenName(String screenName) {
		return twitterRssFeedDao.findAllByScreenName(screenName);
	}

	@Transactional(readOnly = true)
	public List<TwitterRssFeed> findRssFeedsByScreenNameAndUrl(String screenName, String url) {
		return twitterRssFeedDao.findAllByScreenNameAndUrl(screenName, url);
	}	

	@Transactional(readOnly = true)
	public TwitterRssFeed findRssFeedsByScreenNameAndId(String screenName, Integer id) {
		return twitterRssFeedDao.findByScreenNameAndId(screenName, id);
	}	
	
	@Transactional(readOnly = true)
	public List<TwitterRssFeed> findRssFeeds() {
		return twitterRssFeedDao.findAll();
	}
	
	@Transactional(readOnly = true)
	public List<TwitterStatusLog> findStatusLogsByScreenName(String screenName) {
		return twitterStatusLogDao.findAllByScreenName(screenName);
	}

	@Transactional(readOnly = true)
	public List<TwitterStatusLog> findLastOkStatusLogsByScreenName(String screenName, int limit) {
		return twitterStatusLogDao.findLastOkByScreenName(screenName, limit);
	}	

	@Transactional(readOnly = true)
	public List<TwitterStatusLog> findLastStatusLogs(int limit) {
		return twitterStatusLogDao.findLast(limit);
	}	
	
	@Transactional(readOnly = true)	
	public boolean alreadySent(String screenName, String url) {
		return twitterStatusLogDao.findOkByScreenNameAndUrl(screenName, url) != null;
	}

	@Transactional(readOnly = true)	
	public TwitterStatusLog findErrorStatus(String screenName, String url) {
		return twitterStatusLogDao.findErrorByScreenNameAndUrl(screenName, url);
	}	
	
	@Transactional(readOnly = true)	
	public TwitterStatusLog getStatusLogById(Integer id) {
		return twitterStatusLogDao.findById(id);
	}	
	
	@Transactional
	public void updateAccount(TwitterAccount twitterAccount) {
		twitterAccountDao.update(twitterAccount);
	}

	@Transactional
	public void saveRssFeed(TwitterRssFeed twitterRssFeed) {
		twitterRssFeedDao.save(twitterRssFeed);
	}

	@Transactional
	public void updateRssFeed(TwitterRssFeed twitterRssFeed) {
		twitterRssFeedDao.update(twitterRssFeed);
	}

	@Transactional
	public void deleteRssFeed(TwitterRssFeed twitterRssFeed) {
		twitterRssFeedDao.delete(twitterRssFeed);
	}

	@Transactional
	public void saveStatusLog(TwitterStatusLog twitterActionStatus) {
		twitterStatusLogDao.save(twitterActionStatus);
	}

	@Transactional
	public void updateStatusLog(TwitterStatusLog twitterActionStatus) {
		twitterStatusLogDao.update(twitterActionStatus);
	}	

	@Transactional
	public void saveOrUpdateStatusLog(TwitterStatusLog twitterActionStatus) {
		if (twitterActionStatus.getId() != null) {
			twitterStatusLogDao.update(twitterActionStatus);
		} else {
			twitterStatusLogDao.save(twitterActionStatus);
		}
	}	
	
	@Transactional
	public TwitterAccount saveAccessToken(AccessToken accessToken) {
		String screenName = accessToken.getScreenName().toLowerCase();
		TwitterAccount account = twitterAccountDao.findByScreenName(screenName);
		if (account == null) {
			account = new TwitterAccount();
			account.setScreenName(screenName);
			account.setAccessToken(accessToken.getToken());
			account.setAccessTokenSecret(accessToken.getTokenSecret());
			twitterAccountDao.save(account);
		} else {
			account.setAccessToken(accessToken.getToken());
			account.setAccessTokenSecret(accessToken.getTokenSecret());
			twitterAccountDao.update(account);
		}
		return account;
	}

	public String generateShortLink(int id) {
		return config.getShortLinkPrefix() + "0" + Integer.toString(id, Character.MAX_RADIX);
	}	
	
}
