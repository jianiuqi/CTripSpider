package com.jxn.ctrip.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抓取小吃数据 http://you.ctrip.com/fooditem/beijing1.html
 * @author jnq
 */
public class Food {

	/**
	 * 小吃id
	 */
	private String food_id;
	
	/**
	 * 小吃名称
	 */
	private String name;
	
	/**
	 * 小吃图片
	 */
	private List<String> imgUrls = new ArrayList<String>();
	
	/**
	 * 小吃介绍
	 */
	private String desc_info;
	
	/**
	 * 小吃详细页
	 */
	private String detailUrl;
	
	/**
	 * 提供该小吃的饭店
	 */
	private Map<String, String> restaurants = new HashMap<String, String>();

	public String getFood_id() {
		return food_id;
	}

	public void setFood_id(String food_id) {
		this.food_id = food_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getImgUrls() {
		return imgUrls;
	}

	public void setImgUrls(List<String> imgUrls) {
		this.imgUrls = imgUrls;
	}

	public String getDesc_info() {
		return desc_info;
	}

	public void setDesc_info(String desc_info) {
		this.desc_info = desc_info;
	}

	public Map<String, String> getRestaurants() {
		return restaurants;
	}

	public void setRestaurants(Map<String, String> restaurants) {
		this.restaurants = restaurants;
	}

	public String getDetailUrl() {
		return detailUrl;
	}

	public void setDetailUrl(String detailUrl) {
		this.detailUrl = detailUrl;
	}

}
