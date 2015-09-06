package com.euromoby.r2t.core.vk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.r2t.core.Config;
import com.euromoby.r2t.core.http.HttpClientProvider;
import com.euromoby.r2t.core.utils.HttpUtils;
import com.euromoby.r2t.core.vk.model.VkAccount;
import com.euromoby.r2t.core.vk.model.json.VkGroupResponse;
import com.google.gson.Gson;

@Component
public class VkProvider {
	private static final String API_VERSION = "5.37";

	private static final String AUTH_URL = "https://oauth.vk.com/authorize" + "?client_id={APP_ID}" + "&scope={PERMISSIONS}" + "&redirect_uri={REDIRECT_URI}"
			+ "&display={DISPLAY}" + "&v={API_VERSION}" + "&response_type=token";

	private static final String API_REQUEST = "https://api.vk.com/method/{METHOD_NAME}" + "?{PARAMETERS}" + "&access_token={ACCESS_TOKEN}" + "&v="
			+ API_VERSION;

	private static final Gson GSON = new Gson();	
	
	@Autowired
	private Config config;
	
	@Autowired
	private HttpClientProvider httpClientProvider;

	public String getAuthorizationUrl(String permissions) {
		String reqUrl = AUTH_URL.replace("{APP_ID}", config.getVkAppId())
				.replace("{PERMISSIONS}", permissions)
				.replace("{REDIRECT_URI}", "https://oauth.vk.com/blank.html")
				.replace("{DISPLAY}", "page")
				.replace("{API_VERSION}", API_VERSION);

		return reqUrl;
	}
	
	public String invokeApi(String accessToken, String methodName, Map<String, String> params) throws IOException {
		
		String reqUrl = API_REQUEST.replace("{METHOD_NAME}", methodName)
				.replace("{ACCESS_TOKEN}", accessToken);
		
		StringBuilder paramsStringBuilder = new StringBuilder();
		for (String param : params.keySet()) {
			String value = params.get(param); //URLEncoder.encode(params.get(param), "UTF-8");
			paramsStringBuilder.append("&").append(param).append("=").append(value);
		}
		
		String paramsString = paramsStringBuilder.toString();
		// remove first &
		if (paramsString.length() > 0) {
			paramsString = paramsString.substring(1);
		}
		
		reqUrl = reqUrl.replace("{PARAMETERS}", paramsString);
		
		byte[] response = HttpUtils.loadUrl(httpClientProvider, reqUrl);
		
		return new String(response, "UTF-8");
		
	}

	public VkGroupResponse getAdminGroups(VkAccount vkAccount) throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("user_id", vkAccount.getUserId());
		params.put("extended", "1");
		params.put("filter", "admin");
        String response = invokeApi(vkAccount.getAccessToken(), "groups.get", params);
        VkGroupResponse vkGroupResponse = GSON.fromJson(response, VkGroupResponse.class);
        return vkGroupResponse;
    }	

	public String postToWall(VkAccount vkAccount, String ownerId, String message) throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("owner_id", ownerId);
		params.put("from_group", "1");
		params.put("message", message);
        return invokeApi(vkAccount.getAccessToken(), "wall.post", params);
    }	
	
}
