package com.jxn.ctrip.spider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jxn.ctrip.entity.GLPlace;
import com.jxn.ctrip.util.HttpUtil;
import com.jxn.ctrip.util.SqlDBUtils;
import com.jxn.ctrip.util.StringUtil;

/**
 * 抓取攻略地点 http://you.ctrip.com/sitemap/placedis/c110000
 */
public class GLPlaceSpider {

	boolean DEBUG = false;
	
	Connection conn = null;
	PreparedStatement preparedStatement = null;
	
	public static void main(String[] args) {
		GLPlaceSpider spider = new GLPlaceSpider();
		List<GLPlace> places = spider.getGLPlaces();
		spider.saveGLPlace(places);
	}
	
	public List<GLPlace> getGLPlaces() {
		List<GLPlace> places = new ArrayList<GLPlace>();
		String result = HttpUtil.getInstance().httpGet(null, "http://you.ctrip.com/sitemap/placedis/c110000");
		Document doc = Jsoup.parse(result);
		Elements allPlacesEles = doc.select("div.content > div.sitemap_block");
		int count = 1;
		for (Element all_ele : allPlacesEles) {
			// 每个省
			Elements province = all_ele.select("h2");
			Elements moreHrefEles = all_ele.select("div.map_title.cf > a");
			// 每个省下面的地点
			Elements place_eles = all_ele.select("ul.map_linklist.cf > li"); // lis
			for (Element place_ele : place_eles) {
				Element aTag_ele = place_ele.select("a").get(0);// 真正有用的内容在li > a中
				GLPlace place = new GLPlace();
				if (moreHrefEles.size() > 0) {
					place.setProvince(province.get(0).html().trim());
				}
				place.setHref(aTag_ele.attr("href"));
				place.setTitle(aTag_ele.attr("title"));
				place.setPlace(aTag_ele.attr("title").replace("旅游攻略", ""));
				String pinyin_placecode = aTag_ele.attr("href").substring(aTag_ele.attr("href").lastIndexOf("/")+1, 
						aTag_ele.attr("href").lastIndexOf("."));
				place.setPlace_code(StringUtil.getNumbers(pinyin_placecode));
				place.setPinyin(pinyin_placecode.replace(StringUtil.getNumbers(pinyin_placecode), ""));
				places.add(place);
			}
			// 获取更多中的地点
			if (moreHrefEles.size() > 0) {
				String all_province_gl_place = "http://you.ctrip.com" + moreHrefEles.get(0).attr("href");
				String more_result = HttpUtil.getInstance().httpGet(null, all_province_gl_place);
				Document more_doc = Jsoup.parse(more_result);
				Elements liEles = more_doc.select(".sitemap_block > .map_linklist.cf").get(0).select("li");
				for (Element liTag : liEles) {
					GLPlace place = new GLPlace();
					place.setProvince(province.get(0).html().trim());
					Element aTag_ele = liTag.child(0);// 获取a标签
					place.setHref(aTag_ele.attr("href"));
					place.setTitle(aTag_ele.attr("title"));
					place.setPlace(aTag_ele.attr("title").replace("旅游攻略", ""));
					String pinyin_placecode = aTag_ele.attr("href").substring(aTag_ele.attr("href").lastIndexOf("/")+1, 
							aTag_ele.attr("href").lastIndexOf("."));
					place.setPlace_code(StringUtil.getNumbers(pinyin_placecode));
					place.setPinyin(pinyin_placecode.replace(StringUtil.getNumbers(pinyin_placecode), ""));
					places.add(place);
				}
			}
			if (DEBUG) {
				if (count == 2) {
					break;
				}
				count ++;
			}
		}
		removeDuplicate(places);
		return places;
	}
	
	public void removeDuplicate(List<GLPlace> list){   
		HashSet<GLPlace> set = new HashSet<GLPlace>(list);   
		list.clear();   
		list.addAll(set);
	}
	
	public void saveGLPlace(List<GLPlace> places) {
		try {
			conn = SqlDBUtils.getConnection();
			preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS ctrip_gl_place");
			preparedStatement.execute();
			StringBuilder create_table_sql = new StringBuilder();
			create_table_sql.append("create table if not exists ctrip_gl_place "
					+ "(id integer primary key auto_increment, place_id varchar(255) not null, "
					+ "province varchar(255), place varchar(255) not null, "
					+ "pinyin varchar(255) not null, title varchar(255), href varchar(255), UNIQUE (place_id))");
			preparedStatement = conn.prepareStatement(create_table_sql.toString());
			preparedStatement.execute();
		} catch (Exception e) {
			// TODO: handle exception
		}
		StringBuffer insert_sql = new StringBuffer();
		insert_sql.append("insert into ctrip_gl_place (place_id, province, place, pinyin, title, href) values (?, ?, ?, ?, ?, ?)");
		try {
			conn.setAutoCommit(false);
			preparedStatement = conn.prepareStatement(insert_sql.toString());
			for (GLPlace glPlace : places) {
				preparedStatement.setString(1, glPlace.getPlace_code());
				preparedStatement.setString(2, glPlace.getProvince());
				preparedStatement.setString(3, glPlace.getPlace());
				preparedStatement.setString(4, glPlace.getPinyin());
				preparedStatement.setString(5, glPlace.getTitle());
				preparedStatement.setString(6, glPlace.getHref());
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 从数据库中取出攻略地点信息
	 * @return
	 */
	public List<GLPlace> getDBGLPlaces(){
		List<GLPlace> places = new ArrayList<GLPlace>();
		Connection connection = SqlDBUtils.getConnection();
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM ctrip_gl_place");
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				GLPlace place = new GLPlace();
				place.setPlace_code(resultSet.getString("place_id"));
				place.setProvince(resultSet.getString("province"));
				place.setPlace(resultSet.getString("place"));
				place.setPinyin(resultSet.getString("pinyin"));
				place.setTitle(resultSet.getString("title"));
				place.setHref(resultSet.getString("href"));
				places.add(place);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return places;
	}
	
	/**
	 * 从数据库中取出攻略地点信息
	 * @param id
	 * @return
	 */
	public List<GLPlace> getDBGLPlaces(int id){
		List<GLPlace> places = new ArrayList<GLPlace>();
		Connection connection = SqlDBUtils.getConnection();
		PreparedStatement statement = null;
		try {
			if (id > 1) {
				statement = connection.prepareStatement("SELECT * FROM ctrip_gl_place where id > " + id);
			}else {
				statement = connection.prepareStatement("SELECT * FROM ctrip_gl_place");
			}
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				GLPlace place = new GLPlace();
				place.setPlace_code(resultSet.getString("place_id"));
				place.setProvince(resultSet.getString("province"));
				place.setPlace(resultSet.getString("place"));
				place.setPinyin(resultSet.getString("pinyin"));
				place.setTitle(resultSet.getString("title"));
				place.setHref(resultSet.getString("href"));
				places.add(place);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return places;
	}
}
