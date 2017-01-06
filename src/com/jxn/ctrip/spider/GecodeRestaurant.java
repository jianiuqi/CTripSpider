package com.jxn.ctrip.spider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jxn.ctrip.entity.SimRestaurant;
import com.jxn.ctrip.util.HttpUtil;
import com.jxn.ctrip.util.SqlDBUtils;

public class GecodeRestaurant {

	Connection conn;
	
	public GecodeRestaurant(){
		conn = SqlDBUtils.getConnection();
	}
	
	public static void main(String[] args) {
		/*
		ExecutorService pool = Executors.newFixedThreadPool(50);
		GecodeRestaurant gecodeRestaurant = new GecodeRestaurant();
		try {
			for (int i = 0; i < 20; i++) {
				GecodeThread thread = gecodeRestaurant.new GecodeThread(i);
				pool.execute(thread);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		GecodeRestaurant gecodeRestaurant = new GecodeRestaurant();
		try {
			for (int i = 0; i < 20; i++) {
				List<SimRestaurant> list = gecodeRestaurant.getPageRestaurants(i);
				List<SimRestaurant> latlngList = gecodeRestaurant.getBDLatlng(list);
				gecodeRestaurant.savePageRestaurants(latlngList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class GecodeThread extends Thread {

		private int page;
		
		public GecodeThread(int page) {
			this.page = page;
		}
		
	    @Override
	    public void run() {
	    	List<SimRestaurant> list = getPageRestaurants(page);
			List<SimRestaurant> latlngList = getBDLatlng(list);
			savePageRestaurants(latlngList);
	    }

	}
	
	public List<SimRestaurant> getPageRestaurants(int page){
		List<SimRestaurant> restaurants = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			StringBuffer sql = new StringBuffer();
			// 0-8018页 793082
			sql.append("select id, lat, lng from ctrip_shopping where lat > 0 and lng > 0 and id > 15210 limit ").append(page)
					.append(",100");
			statement = conn.prepareStatement(sql.toString());
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				SimRestaurant r = new SimRestaurant();
				r.setId(resultSet.getInt("id"));
				r.setLat(resultSet.getDouble("lat"));
				r.setLng(resultSet.getDouble("lng"));
				restaurants.add(r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return restaurants;
	}
	
	public void savePageRestaurants(List<SimRestaurant> list){
		PreparedStatement statement = null;
		StringBuffer sql = new StringBuffer();
		sql.append("update ctrip_shopping set bd_lat = ?").append(",");
		sql.append(" bd_lng = ?").append(" where id = ?");
		try {
			conn.setAutoCommit(false);
			statement = conn.prepareStatement(sql.toString()); 
			for (SimRestaurant simRestaurant : list) {
				statement.setDouble(1, simRestaurant.getBd_lat());
				statement.setDouble(2, simRestaurant.getBd_lng());
				statement.setInt(3, simRestaurant.getId());
				statement.addBatch();
			}
			statement.executeBatch();
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * rawlist小于等于100
	 * @return
	 */
	public List<SimRestaurant> getBDLatlng(List<SimRestaurant> rawlist) {
		if (rawlist.size() > 100) {
			return rawlist;
		}
		HashMap<String, String> params = new HashMap<>();
		boolean isAppend = false;
		StringBuffer sb = new StringBuffer();
		for (SimRestaurant simRestaurant : rawlist) {
			if (isAppend) {
				sb.append(";");
			}
			sb.append(simRestaurant.getLng()).append(",").append(simRestaurant.getLat());
			isAppend = true;
		}
		params.put("coords", sb.toString());
		params.put("from", "3");
		params.put("to", "5");
		//2 iizeBd0GIyPaNjHBtHKIF0MlQc4XodZW
		//3 jfKsTycc4kKznrXBuqywOPGnkrHPuGfi
		//4 lwhAmCzsw5oDv9E24KAfKaw6kdg7Qqen
		//5 bGXBbclRZrOAH9LOgwVTXRHa0zE0tkkx
		//6 U9prkxIa2OIWktByUHj88GXEGsBb65XD
		//7 ex58ptRRxaafn8qaYuOVRhOcbHjeM0QO
		//8 A1xYQpfD38FoNz3332XrwPxqzsBBmaly
		params.put("ak", "A1xYQpfD38FoNz3332XrwPxqzsBBmaly");
		String result = HttpUtil.getInstance().httpGet(params , "http://api.map.baidu.com/geoconv/v1/");
		if (result != null) {
			JSONObject object = JSONObject.parseObject(result);
			if (object != null && object.getInteger("status") == 0) {
				JSONArray array = object.getJSONArray("result");
				for (int i = 0; i < array.size(); i++) {
					JSONObject bd_obj = array.getJSONObject(i);
					SimRestaurant r = rawlist.get(i);
					r.setBd_lng(bd_obj.getDoubleValue("x"));
					r.setBd_lat(bd_obj.getDoubleValue("y"));
				}
			}
		}
		return rawlist;
	}
}
