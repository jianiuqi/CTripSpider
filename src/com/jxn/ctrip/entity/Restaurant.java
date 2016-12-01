package com.jxn.ctrip.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小吃的饭店
 * @author jnq
 *
 */
public class Restaurant {

	/**
	 * 饭店Id
	 */
	private String restaurant_id = "";
	
	/**
	 * 饭店名称
	 */
	private String name = "";
	
	/**
	 * 人均消费
	 */
	private double avgPrice;
	
	/**
	 * 菜系
	 */
	private String foodType = "";
	
	/**
	 * 电话
	 */
	private String tel = "";
	
	/**
	 * 地址
	 */
	private String address = "";
	
	/**
	 * 营业时间
	 */
	private String openTime = "";
	
	/**
	 * 饭店描述
	 */
	private String desc_info = "";
	
	/**
	 * 餐厅介绍
	 */
	private String detail = "";
	
	/**
	 * 本店特色美食
	 */
	private String famous = "";
	
	/**
	 * 评分
	 */
	private double score;
	
	/**
	 * 纬度
	 */
	private double lat;
	
	/**
	 * 经度
	 */
	private double lng;
	
	/**
	 * 餐馆详细页地址
	 */
	private String detailUrl = "";
	
	/**
	 * 推荐菜品
	 */
	private Map<String, String> suggestFoods = new HashMap<String, String>();
	
	/**
	 * 饭店图片
	 */
	private List<String> imgUrls = new ArrayList<String>();
	
	public String getRestaurant_id() {
		return restaurant_id;
	}

	public void setRestaurant_id(String restaurant_id) {
		this.restaurant_id = restaurant_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc_info() {
		return desc_info;
	}

	public void setDesc_info(String desc_info) {
		this.desc_info = desc_info;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getFamous() {
		return famous;
	}

	public void setFamous(String famous) {
		this.famous = famous;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(double avgPrice) {
		this.avgPrice = avgPrice;
	}

	public String getFoodType() {
		return foodType;
	}

	public void setFoodType(String foodType) {
		this.foodType = foodType;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getOpenTime() {
		return openTime;
	}

	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}

	public Map<String, String> getSuggestFoods() {
		return suggestFoods;
	}

	public void setSuggestFoods(Map<String, String> suggestFoods) {
		this.suggestFoods = suggestFoods;
	}

	public String getDetailUrl() {
		return detailUrl;
	}

	public void setDetailUrl(String detailUrl) {
		this.detailUrl = detailUrl;
	}

	public List<String> getImgUrls() {
		return imgUrls;
	}

	public void setImgUrls(List<String> imgUrls) {
		this.imgUrls = imgUrls;
	}
	
}
