package com.euromoby.r2t.core.http;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.r2t.core.Config;
import com.euromoby.r2t.core.utils.StringUtils;


@Component
public class HttpClientProvider implements DisposableBean {

	private Config config;
	private CloseableHttpClient httpClient;

	@Autowired	
	public HttpClientProvider(Config config){
		this.config = config;
		this.httpClient = createHttpClient();
	}
	
	protected CloseableHttpClient createHttpClient() {
		return HttpClientBuilder.create()
				.setUserAgent(config.getHttpUserAgent())
				.build();
	}

	public RequestConfig.Builder createRequestConfigBuilder() {
		
		int timeout = config.getClientTimeout();
		
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder.setSocketTimeout(timeout);
		requestConfigBuilder.setConnectTimeout(timeout);
		
		if (!StringUtils.nullOrEmpty(config.getProxyHost())) {
			requestConfigBuilder.setProxy(new HttpHost(config.getProxyHost(), config.getProxyPort()));
		}
		return requestConfigBuilder;
	}

	public CloseableHttpResponse executeRequest(HttpUriRequest httpRequest) throws ClientProtocolException, IOException {
		return httpClient.execute(httpRequest);
	}
	
	
	@Override
	public void destroy() throws Exception {
		IOUtils.closeQuietly(httpClient);
	}
	
}
