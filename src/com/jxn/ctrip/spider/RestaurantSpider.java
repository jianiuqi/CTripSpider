package com.jxn.ctrip.spider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jxn.ctrip.entity.GLPlace;
import com.jxn.ctrip.entity.Restaurant;
import com.jxn.ctrip.util.HttpUtil;
import com.jxn.ctrip.util.SqlDBUtils;

/**
 * 抓取饭店数据
 * @author jnq
 *
 */
public class RestaurantSpider {

	Connection conn = null;
	PreparedStatement preparedStatement = null;
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		RestaurantSpider spider = new RestaurantSpider();
		GLPlaceSpider placeSpider = new GLPlaceSpider();
		List<GLPlace> places = placeSpider.getDBGLPlaces();
		spider.createTable();
		for (GLPlace place : places) {
			spider.getRestanrants(place);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("获取饭店所用时间(ms):" + (endTime - startTime));
	}
	
	public List<Restaurant> getRestanrants(GLPlace place){
		List<Restaurant> restaurants = new ArrayList<Restaurant>();
		String html = HttpUtil.getInstance().httpGet(null, "http://you.ctrip.com/restaurantlist/" + place.getPinyin() + place.getPlace_code() + ".html");
		Document doc = Jsoup.parse(html);
		getPageRestaurants(doc, restaurants, place);
		return restaurants;
	}
	
	/**
	 * 获取每页结果
	 * @param doc
	 * @param restaurants
	 */
	public void getPageRestaurants(Document doc, List<Restaurant> restaurants, GLPlace place){
		Elements restaurants_containers = doc.getElementsByClass("list_wide_mod2");
		if (restaurants_containers.size() > 0) {
			Element foods_container = restaurants_containers.first();
			// 获取list_mod2 div包裹的内容
			Elements foodsListMod = foods_container.getElementsByClass("list_mod2");
			for (Element rest_ele : foodsListMod) {
				Restaurant restaurant = new Restaurant();
				if (rest_ele.select("div.rdetailbox > dl").size() > 0) {
					Element restaurant_infos_ele = rest_ele.select("div.rdetailbox > dl").first();
					if (restaurant_infos_ele.select("dt > a").size() > 0) {
						String detailUrl = restaurant_infos_ele.select("dt > a").first().attr("href");
						restaurant.setDetailUrl(detailUrl);// 饭店详细地址页
						
						// 获取详细页信息
						String restDetailHtml = HttpUtil.getInstance().httpGet(null, "http://you.ctrip.com" + detailUrl);
						Document detail_doc = Jsoup.parse(restDetailHtml);
						// >从直接子元素中查找  空格是从所有子元素中查找
						if (detail_doc.select(".dest_toptitle.detail_tt h1").size() > 0) {
							String rest_name = detail_doc.select(".dest_toptitle.detail_tt h1").first().html();
							restaurant.setName(rest_name); // 饭店名称
						}
						Elements rest_imgs_eles = detail_doc.select("div.carousel-inner > div.item img");
						for (Element img_ele : rest_imgs_eles) {
							restaurant.getImgUrls().add(img_ele.attr("src")); // 饭店图片
						}
						if (detail_doc.select("ul.detailtop_r_info > li > span.score > b").size() > 0) {
							restaurant.setScore(Double.valueOf(detail_doc.select("ul.detailtop_r_info > li > span.score > b").first().html()));// 评分
						}
						if (detail_doc.select("ul.detailtop_r_info > li.infotext").size() > 0 && detail_doc.select("ul.detailtop_r_info > li.infotext > p > a").size() > 0) {
							restaurant.setDesc_info(detail_doc.select("ul.detailtop_r_info > li.infotext").first().ownText()); //餐厅简短介绍
						}
						// 餐厅信息
						Elements info_eles = detail_doc.select("ul.s_sight_in_list.s_sight_noline.cf > li");
						for (Element info_ele : info_eles) {
							String info_title = info_ele.select("span.s_sight_classic").first().ownText();
							if ( info_title != null && info_title.contains("人 均") && info_ele.select("span.s_sight_con > em").size() > 0) {
								String price = info_ele.select("span.s_sight_con > em").first().ownText().replace("¥", "").replace("暂无", "");
								restaurant.setAvgPrice(Double.valueOf(price));
							}else if ( info_title != null && info_title.contains("菜 系") && info_ele.select("span.s_sight_con > dd > a").size() > 0) {
								String foodType = info_ele.select("span.s_sight_con > dd > a").first().ownText().replace("暂无", "");
								restaurant.setFoodType(foodType);
							}else if ( info_title != null && info_title.contains("电 话") && info_ele.select("span.s_sight_con").size() > 0) {
								String tel = info_ele.select("span.s_sight_con").first().ownText().replace("暂无", "");
								restaurant.setTel(tel);
							}else if ( info_title != null && info_title.contains("地 址") && info_ele.select("span.s_sight_con").size() > 0) {
								String address = info_ele.select("span.s_sight_con").first().ownText().replace("暂无", "");
								restaurant.setAddress(address);
							}else if ( info_title != null && info_title.contains("营业时间") && info_ele.select("span.s_sight_con").size() > 0) {
								String openTime = info_ele.select("span.s_sight_con").first().ownText().replace("暂无", "");
								restaurant.setOpenTime(openTime);
							}
						}
						// 餐厅介绍    本店特色美食
						Elements detail_famous_eles = detail_doc.select("div.normalbox > div.detailcon > div.text_style");
						for (Element detail_famous_ele : detail_famous_eles) {
							String preElementText = detail_famous_ele.previousElementSibling().ownText();
							if ("餐厅介绍".equals(preElementText)) {
								restaurant.setDetail(detail_famous_ele.ownText());
							}else if("本店特色美食".equals(preElementText)) {
								if (detail_famous_ele.select("p").size() > 0) {
									restaurant.setFamous(detail_famous_ele.select("p").first().ownText());
								}
							}
						}
					}
				}
				if (rest_ele.select("div.abiconbox").size() > 0) {
					String restaurant_id = rest_ele.select("div.abiconbox").first().attr("data-id");
					restaurant.setRestaurant_id(restaurant_id);// 饭店id
				}
				// 网友最爱吃
				Elements suggest_foods_eles = rest_ele.select("p.bottomcomment.ellipsis");
				if (suggest_foods_eles.size() > 0) {
					Elements suggest_foods_aTag = suggest_foods_eles.first().select("a");
					Map<String, String> foods = restaurant.getSuggestFoods();
					for (Element suggest_food : suggest_foods_aTag) {
						foods.put(suggest_food.attr("data-id"), suggest_food.ownText());
					}
				}
			    // 饭店经纬度(腾讯坐标)
				if (restaurant.getName() != null && !"".equals(restaurant.getName())) {
					double[] lat_lng = getLatlng(restaurant.getName(), place.getPlace());
					restaurant.setLat(lat_lng[0]);
					restaurant.setLng(lat_lng[1]);
				}
				saveRestaurant(restaurant, place);
				restaurants.add(restaurant);
			}
			// 下一页数据
			Elements pager_container_eles = doc.select("div.ttd_pager.cf");
			if (pager_container_eles.size() > 0) {
				Element pager_container_ele = pager_container_eles.first();
				if (pager_container_ele.select(".nextpage").size() > 0) {
					Element next_page_ele = pager_container_ele.select(".nextpage").first();
					if (!"nextpage disabled".equals(next_page_ele.className())) {
						String html = HttpUtil.getInstance().httpGet(null, "http://you.ctrip.com" + next_page_ele.attr("href"));
						Document doc_next = Jsoup.parse(html);
						getPageRestaurants(doc_next, restaurants, place);
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
			preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS ctrip_restaurant");
			preparedStatement.execute();
			StringBuilder create_table_sql = new StringBuilder();
			create_table_sql.append("create table if not exists ctrip_restaurant "
					+ "(id integer primary key auto_increment, restaurant_id varchar(255) not null, "
					+ "place_id varchar(255) not null, place_name varchar(255) not null, "
					+ "name varchar(255) not null, avgPrice double, foodType varchar(255), tel varchar(255), "
					+ "address varchar(255), openTime varchar(255), desc_info varchar(255), "
					+ "detail text, famous text, score double, "
					+ "lat double, lng double, detailUrl varchar(255), suggestFoods text, "
					+ "imgUrls text, UNIQUE (restaurant_id))");
			preparedStatement = conn.prepareStatement(create_table_sql.toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveRestaurant(Restaurant restaurant, GLPlace place){
		StringBuffer insert_sql = new StringBuffer();
		insert_sql.append("insert into ctrip_restaurant "
				+ "(restaurant_id, place_id, place_name, name, avgPrice, foodType, tel, address, "
				+ "openTime, desc_info, detail, famous, score, lat, lng, "
				+ "detailUrl, suggestFoods, imgUrls) values (");
		insert_sql.append("'" + restaurant.getRestaurant_id() + "'");
		insert_sql.append(",'" + place.getPlace_code() + "'");
		insert_sql.append(", '" + place.getPlace() + "'");
		insert_sql.append(", '" + restaurant.getName() + "'");
		insert_sql.append(", " + restaurant.getAvgPrice());
		insert_sql.append(", '" + restaurant.getFoodType() + "'");
		insert_sql.append(", '" + restaurant.getTel() + "'");
		insert_sql.append(", '" + restaurant.getAddress() + "'");
		insert_sql.append(", '" + restaurant.getOpenTime() + "'");
		insert_sql.append(", '" + restaurant.getDesc_info() + "'");
		insert_sql.append(", '" + restaurant.getDetail()+ "'");
		insert_sql.append(", '" + restaurant.getFamous() + "'");
		insert_sql.append(", " + restaurant.getScore());
		insert_sql.append(", " + restaurant.getLat());
		insert_sql.append(", " + restaurant.getLng());
		insert_sql.append(", '" + restaurant.getDetailUrl() + "'");
		Map<String, String> suggestFoods = restaurant.getSuggestFoods();
		Set<Entry<String, String>> entrySet = suggestFoods.entrySet();
		boolean isAppend = false;
		insert_sql.append(", '");
		for (Entry<String, String> entry : entrySet) {
			if (isAppend) {
				insert_sql.append(";");	
			}
			insert_sql.append(entry.getKey() + ":" + entry.getValue());
			isAppend = true;
		}
		insert_sql.append("'");
		List<String> imgUrls = restaurant.getImgUrls();
		boolean isUrlAppend = false;
		insert_sql.append(", '");
		for (String url : imgUrls) {
			if (isUrlAppend) {
				insert_sql.append(";");
			}
			insert_sql.append(url);
			isUrlAppend = true;
		}
		insert_sql.append("')");
		try {
			preparedStatement = conn.prepareStatement(insert_sql.toString());
			preparedStatement.execute();
		} catch (Exception e) {
			e.getMessage();
		}
	}
	
	public double[] getLatlng(String poiName, String city){
		double[] lat_lng = new double[2];
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("qt", "poi");
		params.put("wd", poiName);
		params.put("pn", "0");
		params.put("rn", "10");
//		params.put("rich_source", "qipao");
		params.put("rich", "web");
		params.put("nj", "0");
		params.put("c", city);
		params.put("key", "d84d6d83e0e51e481e50454ccbe8986b");
		params.put("output", "json");
		params.put("pf", "jsapi");
		params.put("ref", "jsapi");
//		params.put("cb", "qq.maps._svcb3.search_service_0");
		String result = HttpUtil.getInstance().httpGet(params, "http://apis.map.qq.com/jsapi");
		try {
			JSONObject object = JSONObject.parseObject(result);
			if (object != null && !object.isEmpty()) {
				JSONObject detailObj = object.getJSONObject("detail");
				if (detailObj!=null && !detailObj.isEmpty()) {
					JSONArray poisArray = detailObj.getJSONArray("pois");
					// 此处需非空判断
					if (poisArray != null && !poisArray.isEmpty()) {
						for(int i = 0; i < poisArray.size(); i++) {
							JSONObject poi = poisArray.getJSONObject(i);
							double lng = poi.getDouble("pointx");
							double lat = poi.getDouble("pointy");
							if ( lng> 0 &&  lat> 0) {
								lat_lng[0] = lat;
								lat_lng[1] = lng;
								// 转为百度地图坐标
								return lat_lng;
							}else {
								continue;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return lat_lng;
	}
}
