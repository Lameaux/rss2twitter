package com.euromoby.r2t.core.vk.model.json;

public class VkGroup {

	private String id;
	private String name;
	private String screen_name;
	private Integer is_closed;
	private String type;
	private Integer is_admin;
	private Integer admin_level;
	private Integer is_member;
	private String photo_50;
	private String photo_100;
	private String photo_200;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScreen_name() {
		return screen_name;
	}

	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}

	public Integer getIs_closed() {
		return is_closed;
	}

	public void setIs_closed(Integer is_closed) {
		this.is_closed = is_closed;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getIs_admin() {
		return is_admin;
	}

	public void setIs_admin(Integer is_admin) {
		this.is_admin = is_admin;
	}

	public Integer getAdmin_level() {
		return admin_level;
	}

	public void setAdmin_level(Integer admin_level) {
		this.admin_level = admin_level;
	}

	public Integer getIs_member() {
		return is_member;
	}

	public void setIs_member(Integer is_member) {
		this.is_member = is_member;
	}

	public String getPhoto_50() {
		return photo_50;
	}

	public void setPhoto_50(String photo_50) {
		this.photo_50 = photo_50;
	}

	public String getPhoto_100() {
		return photo_100;
	}

	public void setPhoto_100(String photo_100) {
		this.photo_100 = photo_100;
	}

	public String getPhoto_200() {
		return photo_200;
	}

	public void setPhoto_200(String photo_200) {
		this.photo_200 = photo_200;
	}

	@Override
	public String toString() {
		return "VkGroup [id=" + id + ", name=" + name + ", screen_name=" + screen_name + ", is_closed=" + is_closed + ", type=" + type + ", is_admin="
				+ is_admin + ", admin_level=" + admin_level + ", is_member=" + is_member + ", photo_50=" + photo_50 + ", photo_100=" + photo_100
				+ ", photo_200=" + photo_200 + "]";
	}

}
