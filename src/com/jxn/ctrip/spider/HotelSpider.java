package com.jxn.ctrip.spider;

import java.util.HashMap;

import com.jxn.ctrip.util.HttpUtil;

public class HotelSpider {

	public static void main(String[] args) {
		HotelSpider spider = new HotelSpider();
		spider.getHotelList();
	}
	
	public void getHotelList(){
		String url = "http://hotels.ctrip.com/Domestic/Tool/AjaxHotelList.aspx";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("cityName", "宝鸡");
		params.put("cityId", "112");
		params.put("cityPY", "baoji");
		params.put("page", "2");
		String result = HttpUtil.getInstance().httpPost(url, params);
		System.out.println(result);
	}
	
	public void getHotelListTest(){
		String url = "http://hotels.ctrip.com/Domestic/Tool/AjaxHotelList.aspx";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("__VIEWSTATEGENERATOR", "DB1FBB6D");
		params.put("cityName", "宝鸡");
		params.put("StartTime", "2016-11-23");
		params.put("DepTime", "2016-11-24");
		params.put("txtkeyword", "");
		params.put("Resource", "");
		params.put("Room", "");
		params.put("Paymentterm", "");
		params.put("BRev", "");
		params.put("Minstate", "");
		params.put("PromoteType", "");
		params.put("PromoteDate", "");
		params.put("operationtype", "NEWHOTELORDER");
		params.put("PromoteStartDate", "");
		params.put("PromoteEndDate", "");
		params.put("OrderID", "");
		params.put("RoomNum", "");
		params.put("IsOnlyAirHotel", "F");
		params.put("cityId", "112");
		params.put("cityPY", "baoji");
		params.put("cityCode", "0917");
		params.put("cityLat", "34.368996122247");
		params.put("cityLng", "107.24431939654");
		params.put("positionArea", "");
		params.put("positionId", "");
		params.put("keyword", "");
		params.put("hotelId", "");
		params.put("htlPageView", "0");
		params.put("hotelType", "F");
		params.put("hasPKGHotel", "F");
		params.put("requestTravelMoney", "F");
		params.put("isusergiftcard", "F");
		params.put("useFG", "F");
		params.put("HotelEquipment", "");
		params.put("priceRange", "-2");
		params.put("hotelBrandId", "");
		params.put("promotion", "F");
		params.put("prepay", "F");
		params.put("IsCanReserve", "F");
		params.put("OrderBy", "99");
		params.put("OrderType", "");
		params.put("k1", "");
		params.put("k2", "");
		params.put("CorpPayType", "");
		params.put("viewType", "");
		params.put("checkIn", "2016-11-23");
		params.put("checkOut", "2016-11-24");
		params.put("DealSale", "");
		params.put("ulogin", "");
		params.put("hidTestLat", "0%257C0");
		params.put("AllHotelIds", "1532238%252C1210641%252C482267%252C667435%252C533623%252C4016750%252C1597803%252C845942%252C803826%252C533132%252C2612551%252C828063%252C2077752%252C2131340%252C2309298%252C876811%252C486169%252C828076%252C828078%252C5007362%252C4545443%252C1215980%252C1366484%252C975364%252C2306650");// TODO
		params.put("psid", "");
		params.put("HideIsNoneLogin", "T");
		params.put("isfromlist", "T");
		params.put("ubt_price_key", "htl_search_result_promotion");
		params.put("showwindow", "");
		params.put("defaultcoupon", "");
		params.put("isHuaZhu", "False");
		params.put("hotelPriceLow", "");
		params.put("htlFrom", "hotellist");
		params.put("unBookHotelTraceCode", "");
		params.put("showTipFlg", "");
		params.put("hotelIds", "1532238_1_1%2C1210641_2_1%2C482267_3_1%2C667435_4_1%2C533623_5_1%2C4016750_6_1%2C1597803_7_1%2C845942_8_1%2C803826_9_1%2C533132_10_1%2C2612551_11_1%2C828063_12_1%2C2077752_13_1%2C2131340_14_1%2C2309298_15_1%2C876811_16_1%2C486169_17_1%2C828076_18_1%2C828078_19_1%2C5007362_20_1%2C4545443_21_1%2C1215980_22_1%2C1366484_23_1%2C975364_24_1%2C2306650_25_1");// TODO
		params.put("markType", "1");
		params.put("zone", "");
		params.put("location", "");
		params.put("type", "");
		params.put("brand", "");
		params.put("group", "");
		params.put("feature", "");
		params.put("equip", "");
		params.put("star", "");
		params.put("sl", "");
		params.put("s", "");
		params.put("l", "");
		params.put("price", "");
		params.put("a", "0");
		params.put("keywordLat", "");
		params.put("keywordLon", "");
		params.put("contrast", "0");
		params.put("page", "2");
		params.put("contyped", "0");
		params.put("productcode", "");
		String result = HttpUtil.getInstance().httpPost(url, params);
		System.out.println(result);
	}
}
