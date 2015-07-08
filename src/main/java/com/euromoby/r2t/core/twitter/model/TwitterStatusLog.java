package com.euromoby.r2t.core.twitter.model;


public class TwitterStatusLog {

	public static final int STATUS_NEW = 0;
	public static final int STATUS_OK = 1;
	public static final int STATUS_ERROR = 2;

	private Integer id;
	private String screenName;
	private String message;
	private String url;
	private int status = STATUS_NEW;
	private String errorText;
	private long updated;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

	public long getUpdated() {
		return updated;
	}

	public void setUpdated(long updated) {
		this.updated = updated;
	}

}
