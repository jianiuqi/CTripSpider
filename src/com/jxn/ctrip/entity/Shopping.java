package com.jxn.ctrip.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物地点
 * @author jnq
 *
 */
public class Shopping {

	/**
	 * 购物地id
	 */
	private String shopping_id = "";
	
	/**
	 * 购物点攻略地编码
	 */
	private String place_id = "";
	
	/**
	 * 购物点攻略地名称
	 */
	private String place_name = "";
	
	/**
	 * 购物点名称
	 */
	private String name = "";
	
	/**
	 * 购物点地址
	 */
	private String address = "";
	
	/**
	 * 购物点评分
	 */
	private double score;
	
	/**
	 * 购物点类型
	 */
	private String shopping_type = "";
	
	/**
	 * 购物点电话
	 */
	private String tel = "";
	
	/**
	 * 购物点营业时间
	 */
	private String open_time = "";
	
	/**
	 * 购物点简单点评
	 */
	private String dp_info = "";
	
	/**
	 * 购物点简介
	 */
	private String desc_info = "";
	
	/**
	 * 购物点纬度
	 */
	private double lat;
	
	/**
	 * 购物点经度
	 */
	private double lng;
	
	/**
	 * 详细页地址
	 */
	private String detailUrl;
	
	/**
	 * 购物点图片
	 */
	private List<String> imgUrls = new ArrayList<String>();
	
	/**
	 * 购物特色
	 */
	private Map<String, String> suggestGoods = new HashMap<String, String>();

	public String getShopping_id() {
		return shopping_id;
	}

	public void setShopping_id(String shopping_id) {
		this.shopping_id = shopping_id;
	}

	public String getPlace_id() {
		return place_id;
	}

	public void setPlace_id(String place_id) {
		this.place_id = place_id;
	}

	public String getPlace_name() {
		return place_name;
	}

	public void setPlace_name(String place_name) {
		this.place_name = place_name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getShopping_type() {
		return shopping_type;
	}

	public void setShopping_type(String shopping_type) {
		this.shopping_type = shopping_type;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getOpen_time() {
		return open_time;
	}

	public void setOpen_time(String open_time) {
		this.open_time = open_time;
	}

	public String getDp_info() {
		return dp_info;
	}

	public void setDp_info(String dp_info) {
		this.dp_info = dp_info;
	}

	public String getDesc_info() {
		return desc_info;
	}

	public void setDesc_info(String desc_info) {
		this.desc_info = desc_info;
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

	public Map<String, String> getSuggestGoods() {
		return suggestGoods;
	}

	public void setSuggestGoods(Map<String, String> suggestGoods) {
		this.suggestGoods = suggestGoods;
	}
	
}
