package com.jxn.ctrip.entity;

public class Hotel {

	private String id;
	
	private String city_id;
	
	private String city_name;
	
	private String name;
	
	private double lat;
	
	private double lon;
	
	private String url;
	
	private String img;
	
	private String address;
	
	private double score;
	
	/**
	 * 点评分数
	 */
	private int dpscore;
	
	/**
	 * 点评数量
	 */
	private int dpcount;
	
	private String star;
	
	private String stardesc;
	
	private String shortName;
	
	private boolean isSingleRec;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCity_id() {
		return city_id;
	}

	public void setCity_id(String city_id) {
		this.city_id = city_id;
	}

	public String getCity_name() {
		return city_name;
	}

	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
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

	public int getDpscore() {
		return dpscore;
	}

	public void setDpscore(int dpscore) {
		this.dpscore = dpscore;
	}

	public int getDpcount() {
		return dpcount;
	}

	public void setDpcount(int dpcount) {
		this.dpcount = dpcount;
	}

	public String getStar() {
		return star;
	}

	public void setStar(String star) {
		this.star = star;
	}

	public String getStardesc() {
		return stardesc;
	}

	public void setStardesc(String stardesc) {
		this.stardesc = stardesc;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public boolean getIsSingleRec() {
		return isSingleRec;
	}

	public void setIsSingleRec(boolean isSingleRec) {
		this.isSingleRec = isSingleRec;
	}
	
}
