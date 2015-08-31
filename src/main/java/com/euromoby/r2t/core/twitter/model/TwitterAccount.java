package com.euromoby.r2t.core.twitter.model;

public class TwitterAccount {

	private String screenName;
	private String accessToken;
	private String accessTokenSecret;
	private String suggestedSlug;
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

	public String getSuggestedSlug() {
		return suggestedSlug;
	}

	public void setSuggestedSlug(String suggestedSlug) {
		this.suggestedSlug = suggestedSlug;
	}

	public long getLastFollow() {
		return lastFollow;
	}

	public void setLastFollow(long lastFollow) {
		this.lastFollow = lastFollow;
	}

}
