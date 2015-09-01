package com.euromoby.r2t.core.twitter.model;

public class TwitterAccount {

	private String screenName;
	private String accessToken;
	private String accessTokenSecret;
	private String followScreenName;
	private long lastFollow;

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}

	public String getFollowScreenName() {
		return followScreenName;
	}

	public void setFollowScreenName(String followScreenName) {
		this.followScreenName = followScreenName;
	}

	public long getLastFollow() {
		return lastFollow;
	}

	public void setLastFollow(long lastFollow) {
		this.lastFollow = lastFollow;
	}

}
