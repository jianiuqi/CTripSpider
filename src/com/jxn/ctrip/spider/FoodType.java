package com.jxn.ctrip.spider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jxn.ctrip.util.SqlDBUtils;

public class FoodType {

	Connection conn = null;
	PreparedStatement preparedStatement = null;
	
	public static void main(String[] args) throws SQLException {
		FoodType f = new FoodType();
		List<String> types = f.getFoodTypes();
		for (String type : types) {
			String size = f.getTypeSize(type);
			System.out.println(type + " " + size);
		}
		
	}
	
	String getTypeSize(String foodType) throws SQLException{
		preparedStatement = conn.prepareStatement("SELECT COUNT(id) AS type_size FROM ctrip_restaurant WHERE foodType = '"+foodType+"'");
		ResultSet resultSet = preparedStatement.executeQuery();
		String foodTypeSize = "0";
		while (resultSet.next()) {
			foodTypeSize = resultSet.getString("type_size");
		}
		return foodTypeSize;
	}
	
	List<String> getFoodTypes() throws SQLException {
		conn = SqlDBUtils.getConnection();
		preparedStatement = conn.prepareStatement("SELECT DISTINCT foodType AS foodType FROM ctrip_restaurant");
		ResultSet resultSet = preparedStatement.executeQuery();
		List<String> foodType = new ArrayList<String>();
		while (resultSet.next()) {
			foodType.add(resultSet.getString("foodType"));
		}
		return foodType;
	}
}
