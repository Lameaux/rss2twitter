package com.euromoby.r2t.core.vk.model.json;

public class VkResponseWrapper<T> {

	private VkResponse<T> response;

	public VkResponse<T> getResponse() {
		return response;
	}

	public void setResponse(VkResponse<T> response) {
		this.response = response;
	}

}
