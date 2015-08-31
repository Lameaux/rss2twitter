package com.euromoby.r2t.core.twitter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.Category;
import twitter4j.PagableResponseList;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import com.euromoby.r2t.core.Config;
import com.euromoby.r2t.core.twitter.model.TwitterAccount;
import com.euromoby.r2t.core.utils.StringUtils;

@Component
public class TwitterProvider {

	private Config config;

	private Map<String, String> requestTokens = Collections.synchronizedMap(new LRUMap<String, String>());

	@Autowired
	public TwitterProvider(Config config) {
		this.config = config;
	}

	protected Twitter getTwitter() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		// cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(config.getTwitterKey());
		cb.setOAuthConsumerSecret(config.getTwitterSecret());

		if (!StringUtils.nullOrEmpty(config.getProxyHost())) {
			cb.setHttpProxyHost(config.getProxyHost());
			cb.setHttpProxyPort(config.getProxyPort());
		}
		cb.setHttpConnectionTimeout(config.getClientTimeout());
		cb.setHttpReadTimeout(config.getClientTimeout());

		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		return twitter;
	}

	public String getAuthorizationUrl() throws TwitterException {
		RequestToken requestToken = getTwitter().getOAuthRequestToken();
		requestTokens.put(requestToken.getToken(), requestToken.getTokenSecret());
		return requestToken.getAuthorizationURL();
	}

	public AccessToken getAccessToken(String oauthToken, String oauthVerifier) throws TwitterException {
		String oauthTokenSecret = requestTokens.get(oauthToken);
		if (StringUtils.nullOrEmpty(oauthTokenSecret)) {
			throw new TwitterException("Token is invalid");
		}
		RequestToken requestToken = new RequestToken(oauthToken, oauthTokenSecret);
		AccessToken accessToken = getTwitter().getOAuthAccessToken(requestToken, oauthVerifier);
		return accessToken;
	}

	public Status status(TwitterAccount twitterAccount, String text, InputStream picture) throws TwitterException {
		AccessToken accessToken = new AccessToken(twitterAccount.getAccessToken(), twitterAccount.getAccessTokenSecret());

		Twitter twitter = getTwitter();
		twitter.setOAuthAccessToken(accessToken);

		StatusUpdate statusUpdate = new StatusUpdate(text);
		if (picture != null) {
			statusUpdate.setMedia(String.valueOf(text.hashCode()), picture);
		}
		return twitter.updateStatus(statusUpdate);
	}

	public void follow(TwitterAccount twitterAccount, String screenName) throws TwitterException {
		AccessToken accessToken = new AccessToken(twitterAccount.getAccessToken(), twitterAccount.getAccessTokenSecret());

		Twitter twitter = getTwitter();
		twitter.setOAuthAccessToken(accessToken);

		Relationship relationship = twitter.showFriendship(twitterAccount.getScreenName(), screenName);
		if (!relationship.isSourceFollowingTarget()) {
			twitter.createFriendship(screenName, true);
		}
	}

	public List<User> getFriends(TwitterAccount twitterAccount) throws TwitterException {
		AccessToken accessToken = new AccessToken(twitterAccount.getAccessToken(), twitterAccount.getAccessTokenSecret());

		Twitter twitter = getTwitter();
		twitter.setOAuthAccessToken(accessToken);

		PagableResponseList<User> followingUsers;
		List<User> following = new ArrayList<User>();
		long cursor = -1;
		while (cursor != 0) {
			followingUsers = twitter.getFriendsList(twitterAccount.getScreenName(), cursor, 200);
			for (User user : followingUsers) {
				following.add(user);
			}
			cursor = followingUsers.getNextCursor();
		}

		return following;
	}

	public List<User> getFollowers(TwitterAccount twitterAccount) throws TwitterException {
		AccessToken accessToken = new AccessToken(twitterAccount.getAccessToken(), twitterAccount.getAccessTokenSecret());

		Twitter twitter = getTwitter();
		twitter.setOAuthAccessToken(accessToken);

		PagableResponseList<User> followersUsers;
		List<User> followers = new ArrayList<User>();
		long cursor = -1;
		while (cursor != 0) {
			followersUsers = twitter.getFollowersList(twitterAccount.getScreenName(), cursor, 200);
			for (User user : followersUsers) {
				followers.add(user);
			}
			cursor = followersUsers.getNextCursor();
		}

		return followers;
	}

	public List<Category> getSuggestedUserCategories(TwitterAccount twitterAccount) throws TwitterException {
		AccessToken accessToken = new AccessToken(twitterAccount.getAccessToken(), twitterAccount.getAccessTokenSecret());

		Twitter twitter = getTwitter();
		twitter.setOAuthAccessToken(accessToken);
		List<Category> suggested = new ArrayList<Category>();
		ResponseList<Category> suggestedCategories = twitter.getSuggestedUserCategories();
		for (Category category : suggestedCategories) {
			suggested.add(category);
		}
		return suggested;
	}

	public List<User> getSuggestions(TwitterAccount twitterAccount, String slug) throws TwitterException {
		AccessToken accessToken = new AccessToken(twitterAccount.getAccessToken(), twitterAccount.getAccessTokenSecret());

		Twitter twitter = getTwitter();
		twitter.setOAuthAccessToken(accessToken);
		List<User> suggested = new ArrayList<User>();

		ResponseList<User> suggestedUsers = twitter.getUserSuggestions(slug);
		for (User user : suggestedUsers) {
			suggested.add(user);
		}
		return suggested;
	}

}
