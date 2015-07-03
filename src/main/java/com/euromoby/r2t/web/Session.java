package com.euromoby.r2t.web;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.euromoby.r2t.core.utils.StringUtils;

@Component
@Scope("session")
public class Session implements Serializable {

	private static final long serialVersionUID = 1L;

	private String screenName = null;

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public boolean isNotAuthenticated() {
		return StringUtils.nullOrEmpty(screenName);
	}
	
}
