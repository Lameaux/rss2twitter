package com.euromoby.r2t.core.vk.model.json;

public class VkResponse<T> {

	private Integer count;
	private T[] items;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public T[] getItems() {
		return items;
	}

	public void setItems(T[] items) {
		this.items = items;
	}

}
