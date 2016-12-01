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

import com.jxn.ctrip.entity.GLPlace;
import com.jxn.ctrip.entity.Good;
import com.jxn.ctrip.util.HttpUtil;
import com.jxn.ctrip.util.SqlDBUtils;

/**
 * 抓取特色商品
 * @author jnq
 *
 */
public class GoodSpider {

	Connection conn = null;
	PreparedStatement preparedStatement = null;
	
	public static void main(String[] args) {
		GLPlaceSpider placeSpider = new GLPlaceSpider();
		List<GLPlace> places = placeSpider.getDBGLPlaces();
		GoodSpider goodSpider = new GoodSpider();
		goodSpider.saveGoods(places);
	}
	
	public void saveGoods(List<GLPlace> places){
		createTable();
		for (GLPlace glPlace : places) {
			try {
				getGoods(glPlace);
			} catch (Exception e) {
				System.out.println(glPlace.getPlace());
			}
		}
	}
	
	public List<Good> getGoods(GLPlace place) {
		List<Good> goods = new ArrayList<Good>();
		String html = HttpUtil.getInstance().httpGet(null, "http://you.ctrip.com/goods/" + place.getPinyin() + place.getPlace_code() + ".html");
		Document doc = Jsoup.parse(html);
		getPageGoods(doc, goods, place);
		return goods;
	}
	
	public void getPageGoods(Document doc, List<Good> goods, GLPlace place){
		Elements good_item_eles = doc.select("div.list_wide_mod2 > div.list_mod2.foodlist");
		for (Element good_item_div : good_item_eles) {
			Good good = new Good();
			good.setGood_id(good_item_div.attr("data-id"));
			Elements good_detail_tag_eles = good_item_div.select("div.rdetailbox > dl > dt > a");
			// 设置名称、介绍、图片
			if (good_detail_tag_eles.size() > 0) {
				good.setDetailUrl("http://you.ctrip.com" + good_detail_tag_eles.first().attr("href"));
				String detailHtml = HttpUtil.getInstance().httpGet(null, good.getDetailUrl());
				Document detailDoc = Jsoup.parse(detailHtml);
				if (detailDoc.select("div.detailtop_r_info > ul > li.product_title.ellipsis").size() > 0) {
					good.setName(detailDoc.select("div.detailtop_r_info > ul > li.product_title.ellipsis").first().ownText());
				}
				if (detailDoc.select("div.detailtop_r_info > ul > li.infotext").size() > 0) {
					good.setDesc_info(detailDoc.select("div.detailtop_r_info > ul > li.infotext").first().ownText());
				}
				Elements img_eles = detailDoc.select("#detailCarousel img");
				List<String> goodImgs = good.getImgUrls();
				for (Element img_ele : img_eles) {
					goodImgs.add(img_ele.attr("src"));
				}
				// 设置在哪能买到
				Elements good_shopping_eles = detailDoc.select("div.normalbox.productbox > div.card_list.in_card > ul > li");
				Map<String, String> suggestShoppings = good.getSuggestShoppings();
				for (Element good_shopping_ele : good_shopping_eles) {
					String href = null;
					String title = null;
					if (good_shopping_ele.select("a").size() > 0) {
						href = good_shopping_ele.select("a").first().attr("href");
					}
					if (good_shopping_ele.select("dl > dt > span.ellipsis").size() > 0) {
						title = good_shopping_ele.select("dl > dt > span.ellipsis").first().ownText();
					}
					if (href != null && title != null) {
						suggestShoppings.put(href, title);
					}
				}
				saveGood(good, place);
				goods.add(good);
			}
		}
		// 继续读取下一页信息
		Elements pager_container_eles = doc.select("div.ttd_pager.cf");
		if (pager_container_eles.size() > 0) {
			Element pager_container_ele = pager_container_eles.first();
			if (pager_container_ele.select(".nextpage").size() > 0) {
				Element next_page_ele = pager_container_ele.select(".nextpage").first();
				if (!"nextpage disabled".equals(next_page_ele.className())) {
					String html = HttpUtil.getInstance().httpGet(null,
							"http://you.ctrip.com" + next_page_ele.attr("href"));
					Document doc_next = Jsoup.parse(html);
					getPageGoods(doc_next, goods, place);
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
			preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS ctrip_good");
			preparedStatement.execute();
			StringBuilder create_table_sql = new StringBuilder();
			create_table_sql.append("create table if not exists ctrip_good "
					+ "(id integer primary key auto_increment, good_id varchar(255) not null, "
					+ "place_id varchar(255) not null, place_name varchar(255) not null, "
					+ "name varchar(255) not null, desc_info text, detailUrl varchar(255), "
					+ "imgUrls text, suggestShoppings text, UNIQUE (good_id))");
			preparedStatement = conn.prepareStatement(create_table_sql.toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveGood(Good good, GLPlace place){
		StringBuffer insert_sql = new StringBuffer();
		insert_sql.append("insert into ctrip_good "
				+ "(good_id, place_id, place_name, name, desc_info, detailUrl, imgUrls, suggestShoppings) values (");
		insert_sql.append("'" + good.getGood_id() + "'");
		insert_sql.append(",'" + place.getPlace_code() + "'");
		insert_sql.append(", '" + place.getPlace() + "'");
		insert_sql.append(", '" + good.getName() + "'");
		insert_sql.append(", '" + good.getDesc_info()+ "'");
		insert_sql.append(", '" + good.getDetailUrl() + "'");
		// 保存url ;分隔
		List<String> imgUrls = good.getImgUrls();
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
		// 保存suggestShoppings ;分隔
		Map<String, String> suggestShoppings = good.getSuggestShoppings();
		Set<Entry<String, String>> entrySet = suggestShoppings.entrySet();
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
