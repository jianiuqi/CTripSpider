package com.jxn.ctrip.spider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jxn.ctrip.util.HttpUtil;
import com.jxn.ctrip.util.SqlDBUtils;
import com.jxn.ctrip.util.StringUtil;

public class HotelCitySpider {

	public static void main(String[] args) {
		String result = HttpUtil.getInstance().httpGet(null, "http://hotels.ctrip.com/domestic-city-hotel.html");
		Document root_document = Jsoup.parse(result);
		Elements pinyin_filter_elements = root_document.getElementsByClass("pinyin_filter_detail layoutfix");
		Element pinyin_filter = pinyin_filter_elements.first();
		Elements pinyins = pinyin_filter.getElementsByTag("dt");
		Elements hotelsLinks = pinyin_filter.getElementsByTag("dd");
		// 连接数据库
		Connection conn = SqlDBUtils.getConnection();
		StringBuilder create_table_sql = new StringBuilder();
		create_table_sql.append("create table if not exists ctrip_hotel_city (id integer primary key auto_increment, city_id integer not null, city_name varchar(255) not null, head_pinyin varchar(80) not null, pinyin varchar(255) not null)");
		PreparedStatement preparedStatement;
		try {
			preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS ctrip_hotel_city");
			preparedStatement.execute();
			preparedStatement = conn.prepareStatement(create_table_sql.toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < pinyins.size(); i++) {
			Element head_pinyin = pinyins.get(i);
			Element head_hotelsLink = hotelsLinks.get(i);
			Elements links = head_hotelsLink.children();
			for (Element link : links) {
				String cityId = StringUtil.getNumbers(link.attr("href"));
				String cityName = link.html();
				String head_pinyin_str = head_pinyin.html();
				String pinyin_cityId = link.attr("href").replace("/hotel/", "");
				String pinyin = pinyin_cityId.replace(StringUtil.getNumbers(link.attr("href")), "");
				StringBuffer insert_sql = new StringBuffer();
				insert_sql.append("insert into ctrip_hotel_city (city_id, city_name, head_pinyin, pinyin) values (");
				insert_sql.append(cityId);
				insert_sql.append(", '" + cityName + "'");
				insert_sql.append(", '" + head_pinyin_str + "'");
				//此处注意汉语拼音中会有'，直接插入数据库会报错，要把一个'替换为两个''
				insert_sql.append(", '" + pinyin.replace("'", "''") + "')");
				try {
					preparedStatement = conn.prepareStatement(insert_sql.toString());
					preparedStatement.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
