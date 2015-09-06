package com.euromoby.r2t.web;

import java.io.Serializable;

import com.euromoby.r2t.core.utils.StringUtils;

public class Session implements Serializable {

	private static final long serialVersionUID = 1L;

	private String screenName = null;
	private String vkUserId = "4867035"; //null;

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public boolean isNotAuthenticated() {
		return StringUtils.nullOrEmpty(screenName);
	}

	public boolean isVkNotAuthenticated() {
		return StringUtils.nullOrEmpty(vkUserId);
	}	
	
	public String getVkUserId() {
		return vkUserId;
	}

	public void setVkUserId(String vkUserId) {
		this.vkUserId = vkUserId;
	}

}
