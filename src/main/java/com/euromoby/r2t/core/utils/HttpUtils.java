package com.euromoby.r2t.core.utils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.euromoby.r2t.core.http.HttpClientProvider;

public class HttpUtils {

	public static byte[] loadUrl(HttpClientProvider httpClientProvider, String url) throws IOException {

		HttpGet request = new HttpGet(url);
		RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder();
		request.setConfig(requestConfigBuilder.build());
		CloseableHttpResponse response = httpClientProvider.executeRequest(request);
		try {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				EntityUtils.consumeQuietly(response.getEntity());
				throw new IOException(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			byte[] content = EntityUtils.toByteArray(entity);
			EntityUtils.consumeQuietly(entity);
			return content;
		} finally {
			response.close();
		}
	}	
	
}
