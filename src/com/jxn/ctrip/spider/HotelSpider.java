package com.jxn.ctrip.spider;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jxn.ctrip.entity.Hotel;
import com.jxn.ctrip.entity.HotelCity;
import com.jxn.ctrip.util.HttpUtil;
import com.jxn.ctrip.util.SqlDBUtils;

public class HotelSpider {

	String hotelUrl = "http://hotels.ctrip.com/Domestic/Tool/AjaxHotelList.aspx";
	Connection conn = null;
	PreparedStatement preparedStatement = null;
	
	boolean DEBUG = true;
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		HotelCitySpider citySpider = new HotelCitySpider();
		HotelSpider spider = new HotelSpider();
		List<HotelCity> cities = citySpider.getHotelCities();
		long getCityTime = System.currentTimeMillis();
		System.out.println("获取城市所用时间(ms):" + (getCityTime - startTime));
		spider.createTable();
		for (HotelCity hotelCity : cities) {
			spider.saveHotels(hotelCity, spider.getHotelList(hotelCity));
		}
		long saveHotelTime = System.currentTimeMillis();
		System.out.println("获取酒店并存储所用时间(ms):" + (saveHotelTime - startTime));
	}
	
	/**
	 * 获取每个城市的酒店列表
	 * @param city
	 * @param hotels
	 */
	public List<Hotel> getHotelList(HotelCity city){
		// 获取有多少页数据，分页抓取
		String result = getHotelListString(city, "1");
		// 解析酒店数据
		JSONObject resultObj = JSONObject.parseObject(result);
		int hotelAmount = resultObj.getIntValue("hotelAmount");
		List<Hotel> hotels = new ArrayList<Hotel>();
		int page = hotelAmount%25 == 0 ? hotelAmount/25 : hotelAmount/25 + 1;
		for (int i = 1; i < page + 1; i++) {
			String hotelResult = getHotelListString(city, String.valueOf(i));
			// 解析酒店数据
			try {
				JSONObject hotelResultObj = JSONObject.parseObject(hotelResult);
				hotels.addAll(JSON.parseArray(hotelResultObj.getString("hotelPositionJSON"), Hotel.class));
			} catch (Exception e) {
				e.printStackTrace();
				if (DEBUG) {
					File file = new File("file/testdata.txt");
					FileOutputStream fos;
					try {
						fos = new FileOutputStream(file);
						fos.write(hotelResult.getBytes());
						fos.flush();
						fos.close();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return hotels;
	}
	
	/**
	 * 删除表
	 */
	public void createTable() {
		try {
			conn = SqlDBUtils.getConnection();
			preparedStatement = conn.prepareStatement("DROP TABLE IF EXISTS ctrip_hotel");
			preparedStatement.execute();
			StringBuilder create_table_sql = new StringBuilder();
			create_table_sql.append("create table if not exists ctrip_hotel "
					+ "(id integer primary key auto_increment, hotel_id varchar(255) not null, city_id integer not null, city_name varchar(255) not null, "
					+ "name varchar(255) not null, lat double not null, lon double not null, url varchar(255) not null, "
					+ "img varchar(255) not null, address varchar(255) not null, score double not null, "
					+ "dpscore int not null, dpcount int not null, star varchar(255) not null, "
					+ "stardesc varchar(255) not null, shortName varchar(255) not null, isSingleRec tinyint(1), UNIQUE (hotel_id))");
			preparedStatement = conn.prepareStatement(create_table_sql.toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存每个城市的酒店列表
	 * @param city
	 * @param hotels
	 */
	public void saveHotels(HotelCity city, List<Hotel> hotels) {
		for (Hotel hotel : hotels) {
			StringBuffer insert_sql = new StringBuffer();
			insert_sql.append("insert into ctrip_hotel "
					+ "(hotel_id, city_id, city_name, name, lat, lon, url, img, address, score, dpscore, dpcount, star, stardesc, shortName, isSingleRec) values (");
			insert_sql.append("'" + hotel.getId() + "'");
			insert_sql.append(", " + city.getCityId());
			insert_sql.append(", '" + city.getCityName() + "'");
			insert_sql.append(", '" + hotel.getName() + "'");
			insert_sql.append(", " + hotel.getLat());
			insert_sql.append(", " + hotel.getLon());
			insert_sql.append(", '" + hotel.getUrl() + "'");
			insert_sql.append(", '" + hotel.getImg() + "'");
			insert_sql.append(", '" + hotel.getAddress() + "'");
			insert_sql.append(", " + hotel.getScore());
			insert_sql.append(", " + hotel.getDpscore());
			insert_sql.append(", " + hotel.getDpcount());
			insert_sql.append(", '" + hotel.getStar() + "'");
			insert_sql.append(", '" + hotel.getStardesc() + "'");
			insert_sql.append(", '" + hotel.getShortName() + "'");
			insert_sql.append(", " + hotel.getIsSingleRec() + ")");
			try {
				preparedStatement = conn.prepareStatement(insert_sql.toString());
				preparedStatement.execute();
			} catch (Exception e) {
				e.getMessage();
				continue;
			}
		}
	}
	
	/**
	 * 数据有重复unique约束，不可用
	 * @param city
	 * @param hotels
	 */
	public void saveBigHotels(HotelCity city, List<Hotel> hotels){
		StringBuffer insert_sql = new StringBuffer();
		insert_sql.append("insert into ctrip_hotel (hotel_id, city_id, city_name, "
				+ "name, lat, lon, url, img, address, "
				+ "score, dpscore, dpcount, star, stardesc, "
				+ "shortName, isSingleRec) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		try {
			conn.setAutoCommit(false);
			preparedStatement = conn.prepareStatement(insert_sql.toString()); 
			for (Hotel hotel : hotels) {
				preparedStatement.setString(1, hotel.getId());
				preparedStatement.setInt(2, Integer.valueOf(city.getCityId()));
				preparedStatement.setString(3, city.getCityName());
				preparedStatement.setString(4, hotel.getName());
				preparedStatement.setDouble(5, hotel.getLat());
				preparedStatement.setDouble(6, hotel.getLon());
				preparedStatement.setString(7, hotel.getUrl());
				preparedStatement.setString(8, hotel.getImg());
				preparedStatement.setString(9, hotel.getAddress());
				preparedStatement.setDouble(10, hotel.getScore());
				preparedStatement.setInt(11, hotel.getDpscore());
				preparedStatement.setInt(12, hotel.getDpcount());
				preparedStatement.setString(13, hotel.getStar());
				preparedStatement.setString(14, hotel.getStardesc());
				preparedStatement.setString(15, hotel.getShortName());
				preparedStatement.setBoolean(16, hotel.getIsSingleRec());
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getHotelListString(HotelCity city, String page){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("__VIEWSTATEGENERATOR", "DB1FBB6D");
		params.put("cityName", city.getCityName());
//		params.put("StartTime", "2016-11-24");
//		params.put("DepTime", "2016-11-25");
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
		params.put("cityId", city.getCityId());
		params.put("cityPY", city.getPinyin());
//		params.put("cityCode", "1853");
//		params.put("cityLat", "22.1946");
//		params.put("cityLng", "113.549");
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
//		params.put("checkIn", "2016-11-24");
//		params.put("checkOut", "2016-11-25");
		params.put("DealSale", "");
		params.put("ulogin", "");
		params.put("hidTestLat", "0%7C0");
//		params.put("AllHotelIds", "436450%2C371379%2C396332%2C419374%2C345805%2C436553%2C425997%2C436486%2C436478%2C344977%2C5605870%2C344983%2C371396%2C344979%2C2572033%2C699384%2C425795%2C419823%2C2010726%2C5772619%2C1181591%2C2005951%2C345811%2C371381%2C371377");// TODO
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
//		params.put("hotelIds", "436450_1_1,371379_2_1,396332_3_1,419374_4_1,345805_5_1,436553_6_1,425997_7_1,436486_8_1,436478_9_1,344977_10_1,5605870_11_1,344983_12_1,371396_13_1,344979_14_1,2572033_15_1,699384_16_1,425795_17_1,419823_18_1,2010726_19_1,5772619_20_1,1181591_21_1,2005951_22_1,345811_23_1,371381_24_1,371377_25_1");// TODO
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
		params.put("page", page);
		params.put("contyped", "0");
		params.put("productcode", "");
		String result = HttpUtil.getInstance().httpPost(hotelUrl, params);
		// 数据中有转义符直接转JSON报错，所以这里重新拼接所需要的JSON数据
		String tempHotel = result.substring(result.indexOf("hotelPositionJSON")-1, result.length());
		// 确保截取到的字符串是完整的所以用indexOf("}],"), 加2是因为需要]符号
		String hotelArray = tempHotel.substring(0, tempHotel.indexOf("}],") + 2);
		String tempTotalCount = result.substring(result.indexOf("hotelAmount")-1, result.length());
		String totalCount = tempTotalCount.substring(0, tempTotalCount.indexOf(","));
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append(totalCount);
		sb.append(",");
		sb.append(hotelArray);
		sb.append("}");
		return sb.toString().replace("\\", "");
	}
}
