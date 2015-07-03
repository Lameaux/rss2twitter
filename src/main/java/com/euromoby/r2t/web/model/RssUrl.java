package com.euromoby.r2t.web.model;

import javax.validation.constraints.NotNull;

public class RssUrl {

	@NotNull
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
