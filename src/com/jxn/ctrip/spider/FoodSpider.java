package com.jxn.ctrip.spider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jxn.ctrip.entity.Food;
import com.jxn.ctrip.entity.GLPlace;
import com.jxn.ctrip.util.HttpUtil;
import com.jxn.ctrip.util.SqlDBUtils;

/**
 * 抓取小吃数据
 * @author jnq
 *
 */
public class FoodSpider {

	Connection conn = null;
	PreparedStatement preparedStatement = null;
	
	public static void main(String[] args) {
		GLPlaceSpider placeSpider = new GLPlaceSpider();
		List<GLPlace> places = placeSpider.getDBGLPlaces();
		FoodSpider foodSpider = new FoodSpider();
		foodSpider.createTable();
		for (GLPlace glPlace : places) {
			try {
				foodSpider.getFoods(glPlace);
			} catch (Exception e) {
				System.out.println(glPlace.getPlace());
			}
		}
	}
	
	public List<Food> getFoods(GLPlace place) {
		List<Food> foods = new ArrayList<Food>();
		String html = HttpUtil.getInstance().httpGet(null, "http://you.ctrip.com/fooditem/" + place.getPinyin() + place.getPlace_code() + ".html");
		Document doc = Jsoup.parse(html);
		getPageFoods(doc, foods, place);
		return foods;
	}
	
	public void getPageFoods(Document doc, List<Food> foods, GLPlace place){
		Elements foods_container_eles = doc.select("div.list_wide_mod2.open_popupbox");
		if (foods_container_eles.size() > 0) {
			Elements food_item_eles = foods_container_eles.first().select("div.list_mod2.foodlist");
			for (Element food_item_div : food_item_eles) {
				Food food = new Food();
				food.setFood_id(food_item_div.attr("data-id"));
				Elements food_detail_tag_eles = food_item_div.select("div.rdetailbox > dl > dd > a");
				// 设置美食名、介绍、图片
				if (food_detail_tag_eles.size() > 0) {
					food.setDetailUrl("http://you.ctrip.com" + food_detail_tag_eles.attr("href"));
					String detailHtml = HttpUtil.getInstance().httpGet(null, food.getDetailUrl());
					Document detailDoc = Jsoup.parse(detailHtml);
					if (detailDoc.select("ul.detailtop_r_info.food_info > li.title.ellipsis").size() > 0) {
						food.setName(detailDoc.select("ul.detailtop_r_info.food_info > li.title.ellipsis").first().text());
					}
					if (detailDoc.select("ul.detailtop_r_info.food_info > li.infotext").size() > 0) {
						food.setDesc_info(detailDoc.select("ul.detailtop_r_info.food_info > li.infotext").first().text());
					}
					Elements img_eles = detailDoc.select("div.carousel-inner > div.item > a > img");
					List<String> foodImgs = food.getImgUrls();
					for (Element img_ele : img_eles) {
						foodImgs.add(img_ele.attr("src"));
					}
				}else {
					if (food_item_div.select("div.rdetailbox > dl > dt > a").size() > 0) {
						food.setName(food_item_div.select("div.rdetailbox > dl > dt > a").first().attr("title"));
					}
					if (food_item_div.select("div.rdetailbox > dl > dd").size() > 0) {
						food.setDesc_info(food_item_div.select("div.rdetailbox > dl > dd").first().text());
					}
					if (food_item_div.select("div.leftimg > a > img").size() > 0) {
						food.getImgUrls().add(food_item_div.select("div.leftimg > a > img").first().attr("src"));
					}
				}
				// 设置在哪能吃到
				Elements restaurant_eles = food_item_div.select("p.bottomcomment.ellipsis > a");
				Map<String, String> restaurants = food.getRestaurants();
				for (Element restaurant_ele : restaurant_eles) {
					restaurants.put(restaurant_ele.attr("href"), restaurant_ele.attr("title"));
				}
				saveFood(food, place);
				foods.add(food);
			}
			
			// 继续读取下一页信息
			Elements pager_container_eles = doc.select("div.ttd_pager.cf");
			if (pager_container_eles.size() > 0) {
				Element pager_container_ele = pager_container_eles.first();
				if (pager_container_ele.select(".nextpage").size() > 0) {
					Element next_page_ele = pager_container_ele.select(".nextpage").first();
					if (!"nextpage disabled".equals(next_page_ele.className())) {
						String html = HttpUtil.getInstance().httpGet(null, "http://you.ctrip.com" + next_page_ele.attr("href"));
						Document doc_next = Jsoup.parse(html);
						getPageFoods(doc_next, foods, place);
					}
				}
			}
		}
	}
	
	/**
	 * 创建表
	 */
	public void createTable() {
		try {
			conn = SqlDBUtils.getConnection();
			preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS ctrip_food");
			preparedStatement.execute();
			StringBuilder create_table_sql = new StringBuilder();
			create_table_sql.append("create table if not exists ctrip_food "
					+ "(id integer primary key auto_increment, food_id varchar(255) not null, "
					+ "place_id varchar(255) not null, place_name varchar(255) not null, "
					+ "name varchar(255) not null, desc_info text, detailUrl varchar(255), "
					+ "imgUrls text, restaurants text, UNIQUE (food_id))");
			preparedStatement = conn.prepareStatement(create_table_sql.toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveFood(Food food, GLPlace place){
		StringBuffer insert_sql = new StringBuffer();
		insert_sql.append("insert into ctrip_food "
				+ "(food_id, place_id, place_name, name, desc_info, detailUrl, imgUrls, restaurants) values (");
		insert_sql.append("'" + food.getFood_id() + "'");
		insert_sql.append(",'" + place.getPlace_code() + "'");
		insert_sql.append(", '" + place.getPlace() + "'");
		insert_sql.append(", '" + food.getName() + "'");
		insert_sql.append(", '" + food.getDesc_info()+ "'");
		insert_sql.append(", '" + food.getDetailUrl() + "'");
		// 保存url ;分隔
		List<String> imgUrls = food.getImgUrls();
		boolean isUrlAppend = false;
		insert_sql.append(", '");
		for (String url : imgUrls) {
			if (isUrlAppend) {
				insert_sql.append(";");
			}
			insert_sql.append(url);
			isUrlAppend = true;
		}
		insert_sql.append("'");
		// 保存suggestRests ;分隔
		Map<String, String> suggestRests = food.getRestaurants();
		Set<Entry<String, String>> entrySet = suggestRests.entrySet();
		boolean isAppend = false;
		insert_sql.append(", '");
		for (Entry<String, String> entry : entrySet) {
			if (isAppend) {
				insert_sql.append(";");	
			}
			insert_sql.append(entry.getKey() + ":" + entry.getValue());
			isAppend = true;
		}
		insert_sql.append("')");
		try {
			preparedStatement = conn.prepareStatement(insert_sql.toString());
			preparedStatement.execute();
		} catch (Exception e) {
			e.getMessage();
		}
	}
}
