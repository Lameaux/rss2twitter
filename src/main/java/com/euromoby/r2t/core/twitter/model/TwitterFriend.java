package com.euromoby.r2t.core.twitter.model;

public class TwitterFriend {

	private String screenName;
	private String friendScreenName;

	public TwitterFriend() {
	}

	public TwitterFriend(String screenName, String friendScreenName) {
		this.screenName = screenName;
		this.friendScreenName = friendScreenName;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getFriendScreenName() {
		return friendScreenName;
	}

	public void setFriendScreenName(String friendScreenName) {
		this.friendScreenName = friendScreenName;
	}

}
