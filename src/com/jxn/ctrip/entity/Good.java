package com.jxn.ctrip.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 特色商品
 * @author jnq
 *
 */
public class Good {

	/**
	 * 商品id
	 */
	private String good_id = "";
	
	/**
	 * 商品攻略地编码
	 */
	private String place_id = "";
	
	/**
	 * 商品攻略地名称
	 */
	private String place_name = "";
	
	/**
	 * 特设商品名称
	 */
	private String name = "";
	
	/**
	 * 购物点简介
	 */
	private String desc_info = "";
	
	/**
	 * 详细页地址
	 */
	private String detailUrl;
	
	/**
	 * 特色商品图片
	 */
	private List<String> imgUrls = new ArrayList<String>();
	
	/**
	 * 特色商品购买点
	 */
	private Map<String, String> suggestShoppings = new HashMap<String, String>();

	public String getGood_id() {
		return good_id;
	}

	public void setGood_id(String good_id) {
		this.good_id = good_id;
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

	public String getDesc_info() {
		return desc_info;
	}

	public void setDesc_info(String desc_info) {
		this.desc_info = desc_info;
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

	public Map<String, String> getSuggestShoppings() {
		return suggestShoppings;
	}

	public void setSuggestShoppings(Map<String, String> suggestShoppings) {
		this.suggestShoppings = suggestShoppings;
	}
	
}
