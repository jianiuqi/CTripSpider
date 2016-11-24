package com.jxn.ctrip.spider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jxn.ctrip.entity.HotelCity;
import com.jxn.ctrip.util.HttpUtil;
import com.jxn.ctrip.util.SqlDBUtils;
import com.jxn.ctrip.util.StringUtil;

public class HotelCitySpider {

	public static void main(String[] args) {
		HotelCitySpider spider = new HotelCitySpider();
		spider.saveHotelCities(spider.getHotelCities());
	}
	
	/**
	 * 获取所有地区列表(不一定是市，发现数据中有县级别)
	 */
	public List<HotelCity> getHotelCities(){
		List<HotelCity> cities = new ArrayList<HotelCity>();
		String result = HttpUtil.getInstance().httpGet(null, "http://hotels.ctrip.com/domestic-city-hotel.html");
		Document root_document = Jsoup.parse(result);
		Elements pinyin_filter_elements = root_document.getElementsByClass("pinyin_filter_detail layoutfix");
		// 包含所有城市的Element
		Element pinyin_filter = pinyin_filter_elements.first();
		//拼音首字符Elements
		Elements pinyins = pinyin_filter.getElementsByTag("dt");
		//所有dd的Elements
		Elements hotelsLinks = pinyin_filter.getElementsByTag("dd");
		for (int i = 0; i < pinyins.size(); i++) {
			Element head_pinyin = pinyins.get(i);
			Element head_hotelsLink = hotelsLinks.get(i);
			Elements links = head_hotelsLink.children();
			for (Element link : links) {
				String pinyin_cityId = link.attr("href").replace("/hotel/", "");
				String pinyin = pinyin_cityId.replace(StringUtil.getNumbers(link.attr("href")), "");//截取拼音
				HotelCity city = new HotelCity();
				city.setCityId(StringUtil.getNumbers(link.attr("href"))); //截取cityId
				city.setCityName(link.html());
				city.setHeadPinyin(head_pinyin.html());
				city.setPinyin(pinyin);
				cities.add(city);
			}
		}
		return cities;
	}
	
	/**
	 * 保存所有地区列表
	 */
	public void saveHotelCities(List<HotelCity> cities){
		// 连接数据库
		Connection conn = SqlDBUtils.getConnection();
		StringBuilder create_table_sql = new StringBuilder();
		create_table_sql.append("create table if not exists ctrip_hotel_city "
				+ "(id integer primary key auto_increment, city_id integer not null, "
				+ "city_name varchar(255) not null, head_pinyin varchar(80) not null, "
				+ "pinyin varchar(255) not null, UNIQUE (city_id))");
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS ctrip_hotel_city");
			preparedStatement.execute();
			preparedStatement = conn.prepareStatement(create_table_sql.toString());
			preparedStatement.execute();
			for (HotelCity city : cities) {
				StringBuffer insert_sql = new StringBuffer();
				insert_sql.append("insert into ctrip_hotel_city (city_id, city_name, head_pinyin, pinyin) values (");
				insert_sql.append(city.getCityId());
				insert_sql.append(", '" + city.getCityName() + "'");
				insert_sql.append(", '" + city.getHeadPinyin() + "'");
				//此处注意汉语拼音中会有'，直接插入数据库会报错，要把一个'替换为两个''
				insert_sql.append(", '" + city.getPinyin().replace("'", "''") + "')");
				try {
					preparedStatement = conn.prepareStatement(insert_sql.toString());
					preparedStatement.execute();
				} catch (SQLException e) {
					continue;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
