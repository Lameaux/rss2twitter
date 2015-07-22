package com.euromoby.r2t.core;

public class Config {

	public static final String DEFAULT_HTTP_USERAGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

	private int clientTimeout;
	private String proxyHost;
	private int proxyPort;
	private String twitterKey;
	private String twitterSecret;
	private String httpUserAgent = DEFAULT_HTTP_USERAGENT;
	private String shortLinkPrefix;
	private int taskPoolSize;

	public int getClientTimeout() {
		return clientTimeout;
	}

	public void setClientTimeout(int clientTimeout) {
		this.clientTimeout = clientTimeout;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getTwitterKey() {
		return twitterKey;
	}

	public void setTwitterKey(String twitterKey) {
		this.twitterKey = twitterKey;
	}

	public String getTwitterSecret() {
		return twitterSecret;
	}

	public void setTwitterSecret(String twitterSecret) {
		this.twitterSecret = twitterSecret;
	}

	public String getHttpUserAgent() {
		return httpUserAgent;
	}

	public void setHttpUserAgent(String httpUserAgent) {
		this.httpUserAgent = httpUserAgent;
	}

	public String getShortLinkPrefix() {
		return shortLinkPrefix;
	}

	public void setShortLinkPrefix(String shortLinkPrefix) {
		this.shortLinkPrefix = shortLinkPrefix;
	}

	public int getTaskPoolSize() {
		return taskPoolSize;
	}

	public void setTaskPoolSize(int taskPoolSize) {
		this.taskPoolSize = taskPoolSize;
	}

}
