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
import com.jxn.ctrip.entity.Shopping;
import com.jxn.ctrip.util.HttpUtil;
import com.jxn.ctrip.util.SqlDBUtils;

public class ShoppingSpider {

	Connection conn = null;
	PreparedStatement preparedStatement = null;
	
	public static void main(String[] args) {
		GLPlaceSpider glPlaceSpider = new GLPlaceSpider();
		List<GLPlace> places = glPlaceSpider.getDBGLPlaces();
		ShoppingSpider spider = new ShoppingSpider();
		spider.saveShoppings(places);
	}
	
	public void saveShoppings(List<GLPlace> places){
		createTable();
		for (GLPlace glPlace : places) {
			List<Shopping> shoppings = getShoppings(glPlace);
			saveShopping(shoppings, glPlace);
		}
	}
	
	public List<Shopping> getShoppings(GLPlace place){
		List<Shopping> shoppings = new ArrayList<Shopping>();
		String html = HttpUtil.getInstance().httpGet(null, "http://you.ctrip.com/shoppinglist/" + place.getPinyin() + place.getPlace_code() + ".html");
		Document doc = Jsoup.parse(html);
		getPageShoppings(doc, shoppings, place);
		return shoppings;
	}
	
	/**
	 * 获取每页结果
	 * @param doc
	 * @param restaurants
	 */
	public void getPageShoppings(Document doc, List<Shopping> shoppings, GLPlace place){
		Elements shoppings_containers = doc.getElementsByClass("list_wide_mod2");
		if (shoppings_containers.size() > 0) {
			Element shoppings_container = shoppings_containers.first();
			// 获取list_mod2 div包裹的内容
			Elements shoppingsListMod = shoppings_container.getElementsByClass("list_mod2");
			for (Element shopping_ele : shoppingsListMod) {
				Shopping shopping = new Shopping();
				if (shopping_ele.select("div.rdetailbox > dl").size() > 0) {
					Element shopping_infos_ele = shopping_ele.select("div.rdetailbox > dl").first();
					if (shopping_infos_ele.select("dt > a").size() > 0) {
						String detailUrl = shopping_infos_ele.select("dt > a").first().attr("href");
						shopping.setDetailUrl(detailUrl);// 详细地址页
						String tempId = detailUrl.substring(detailUrl.lastIndexOf("/") + 1);
						shopping.setShopping_id(tempId.substring(0, tempId.indexOf(".html")));// id
						
						// 获取详细页信息
						String detailHtml = HttpUtil.getInstance().httpGet(null, "http://you.ctrip.com" + detailUrl);
						Document detail_doc = Jsoup.parse(detailHtml);
						// >从直接子元素中查找  空格是从所有子元素中查找
						if (detail_doc.select(".dest_toptitle.detail_tt h1 > a").size() > 0) {
							String shopping_name = detail_doc.select(".dest_toptitle.detail_tt h1 > a").first().ownText();
							shopping.setName(shopping_name); // 名称
						}
						Elements rest_imgs_eles = detail_doc.select("div.carousel-inner > div.item img");
						for (Element img_ele : rest_imgs_eles) {
							shopping.getImgUrls().add(img_ele.attr("src")); // 图片
						}
						if (detail_doc.select("ul.detailtop_r_info > li > span.score > b").size() > 0) {
							shopping.setScore(Double.valueOf(detail_doc.select("ul.detailtop_r_info > li > span.score > b").first().ownText()));// 评分
						}
						if (detail_doc.select("ul.detailtop_r_info > li.infotext").size() > 0 && detail_doc.select("ul.detailtop_r_info > li.infotext > p > a").size() > 0) {
							shopping.setDp_info(detail_doc.select("ul.detailtop_r_info > li.infotext").first().ownText()); //餐厅简短介绍
						}
						// 详细信息
						Elements info_eles = detail_doc.select("ul.s_sight_in_list > li");
						for (Element info_ele : info_eles) {
							String info_title = info_ele.select("span.s_sight_classic").first().ownText();
							if ( info_title != null && info_title.contains("址") && info_ele.select("span.s_sight_con").size() > 0) {
								String address = info_ele.select("span.s_sight_con").first().ownText().replace("暂无", "");
								shopping.setAddress(address);
							}else if ( info_title != null && info_title.contains("型") && info_ele.select("span.s_sight_con > a").size() > 0) {
								String shopping_type = info_ele.select("span.s_sight_con > a").first().ownText().replace("暂无", "");
								shopping.setShopping_type(shopping_type);
							}else if ( info_title != null && info_title.contains("话") && info_ele.select("span.s_sight_con").size() > 0) {
								String tel = info_ele.select("span.s_sight_con").first().ownText().replace("暂无", "");
								shopping.setTel(tel);
							}
						}
						//营业时间
						Elements open_time_eles = detail_doc.select("dl.s_sight_in_list");
						if (open_time_eles.size() > 0) {
							String info_title = null;
							if (open_time_eles.first().select("dt").size() > 0) {
								info_title = open_time_eles.first().select("dt").first().ownText();
							}
							if (info_title != null && info_title.contains("营业") && open_time_eles.first().select("dd").size() > 0) {
								String openTime = open_time_eles.first().select("dd").first().ownText().replace("暂无", "");
								shopping.setOpen_time(openTime);
							}
						}
						// 介绍
						Elements desc_info_eles = detail_doc.select("div.normalbox > div.detailcon > div.toggle_l > div.text_style");
						if (desc_info_eles.size() > 0) {
							shopping.setDesc_info(desc_info_eles.first().ownText());
						}
						// 购物特色
						Elements suggest_goods_eles = detail_doc.select("div.detailcon.detailbox_dashed > div.card_list.product_card > ul > li");
						Map<String, String> suggest_goods = shopping.getSuggestGoods();
						for (Element suggest_goods_ele : suggest_goods_eles) {
							Elements detail_a_tag = suggest_goods_ele.select("a");
							if (detail_a_tag.size() > 0) {
								suggest_goods.put(suggest_goods_ele.attr("data-id"), detail_a_tag.first().attr("href"));
							}
						}
					}
					
					// 经纬度(腾讯坐标)
					if (shopping.getName() != null && !"".equals(shopping.getName())) {
						double[] lat_lng = getLatlng(shopping.getName(), place.getPlace());
						shopping.setLat(lat_lng[0]);
						shopping.setLng(lat_lng[1]);
					}
					shoppings.add(shopping);
				}
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
						getPageShoppings(doc_next, shoppings, place);
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
			preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS ctrip_shopping");
			preparedStatement.execute();
			StringBuilder create_table_sql = new StringBuilder();
			create_table_sql.append("create table if not exists ctrip_shopping "
					+ "(id integer primary key auto_increment, shopping_id varchar(255) not null, "
					+ "place_id varchar(255) not null, place_name varchar(255) not null, "
					+ "name varchar(255) not null, address varchar(255), "
					+ "score double, shopping_type varchar(255), tel varchar(255), open_time varchar(255), "
					+ "dp_info text, desc_info text, lat double, lng double, "
					+ "detailUrl varchar(255), imgUrls text, "
					+ "suggestGoods text, UNIQUE (shopping_id))");
			preparedStatement = conn.prepareStatement(create_table_sql.toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveShopping(List<Shopping> shoppings, GLPlace place){
		for (Shopping shopping : shoppings) {
			StringBuffer insert_sql = new StringBuffer();
			insert_sql.append("insert into ctrip_shopping "
					+ "(shopping_id, place_id, place_name, name, address, score, shopping_type, "
					+ "tel, open_time, dp_info, desc_info, lat, lng, detailUrl, imgUrls, suggestGoods) values (");
			insert_sql.append("'" + shopping.getShopping_id() + "'");
			insert_sql.append(",'" + place.getPlace_code() + "'");
			insert_sql.append(", '" + place.getPlace() + "'");
			insert_sql.append(", '" + shopping.getName() + "'");
			insert_sql.append(", '" + shopping.getAddress()+ "'");
			insert_sql.append(", " + shopping.getScore());
			insert_sql.append(", '" + shopping.getShopping_type() + "'");
			insert_sql.append(", '" + shopping.getTel() + "'");
			insert_sql.append(", '" + shopping.getOpen_time() + "'");
			insert_sql.append(", '" + shopping.getDp_info() + "'");
			insert_sql.append(", '" + shopping.getDesc_info()+ "'");
			insert_sql.append(", " + shopping.getLat());
			insert_sql.append(", " + shopping.getLng());
			insert_sql.append(", '" + shopping.getDetailUrl() + "'");
			
			List<String> imgUrls = shopping.getImgUrls();
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
			
			Map<String, String> suggestFoods = shopping.getSuggestGoods();
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
			insert_sql.append("')");
			try {
				preparedStatement = conn.prepareStatement(insert_sql.toString());
				preparedStatement.execute();
			} catch (Exception e) {
				e.getMessage();
			}
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
