package com.jxn.ctrip.spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.jxn.ctrip.util.HttpUtil;

/**
 * 抓取酒店详细数据
 * @author jnq
 *
 */
public class HotelDetailSpider {

	public static void main(String[] args) {
		HotelDetailSpider spider = new HotelDetailSpider();
		spider.getHotelDetail("371379");
	}
	
	void getHotelDetail(String hotelId){
		long starttime = System.currentTimeMillis();
		StringBuffer htmlUrl = new StringBuffer();
		htmlUrl.append("http://hotels.ctrip.com/hotel/").append(hotelId).append(".html");
		String result = HttpUtil.getInstance().httpGet(null, htmlUrl.toString());
		Document document = Jsoup.parse(result);
		Elements elements = document.select("div.list_wide_mod2");
		System.out.println(System.currentTimeMillis() - starttime);
	}
}
