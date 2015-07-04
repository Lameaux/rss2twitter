package com.euromoby.r2t.web.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class RssFeed {

	@NotNull
	private String url;

	@Min(1)
	private int frequency;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

}
