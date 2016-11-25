package com.jxn.ctrip.entity;

/**
 * 旅游攻略地点类
 * 从http://you.ctrip.com/sitemap/spotdis/c0抓取
 */
public class GLPlace {

	private String province;
	
	private String place;
	
	private String title;
	
	private String href;
	
	private String pinyin;
	
	private String place_code;

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getPlace_code() {
		return place_code;
	}

	public void setPlace_code(String place_code) {
		this.place_code = place_code;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) 
			return false;
		else {
			if (obj instanceof GLPlace) {
				GLPlace place = (GLPlace) obj;
				if (place.place_code.equals(this.place_code) 
						&& place.place.equals(this.place)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return place_code.hashCode()*place.hashCode();
	}
}
