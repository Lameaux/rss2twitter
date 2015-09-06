package com.euromoby.r2t.web.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class VkRssFeedDto {

	@NotNull
	private String url;

	@NotNull
	private String wall;

	@Min(1)
	private int frequency;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getWall() {
		return wall;
	}

	public void setWall(String wall) {
		this.wall = wall;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

}
