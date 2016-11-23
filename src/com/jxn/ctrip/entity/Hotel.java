package com.jxn.ctrip.entity;

public class Hotel {

	private String id;
	
	private String name;
	
	private String lat;
	
	private String lon;
	
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
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

	public boolean isSingleRec() {
		return isSingleRec;
	}

	public void setSingleRec(boolean isSingleRec) {
		this.isSingleRec = isSingleRec;
	}
	
}
