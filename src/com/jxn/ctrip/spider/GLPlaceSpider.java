package com.jxn.ctrip.spider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jxn.ctrip.entity.GLPlace;
import com.jxn.ctrip.util.HttpUtil;
import com.jxn.ctrip.util.StringUtil;

/**
 * 抓取攻略地点 http://you.ctrip.com/sitemap/placedis/c110000
 */
public class GLPlaceSpider {

	boolean DEBUG = false;
	
	public static void main(String[] args) {
		GLPlaceSpider spider = new GLPlaceSpider();
		List<GLPlace> places = spider.getGLPlaces();
		System.out.println(places.size());
		spider.removeDuplicate(places);
		System.out.println(places.size());
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
		return places;
	}
	
	public void removeDuplicate(List<GLPlace> list){   
		HashSet<GLPlace> set = new HashSet<GLPlace>(list);   
		list.clear();   
		list.addAll(set);
	}
}
