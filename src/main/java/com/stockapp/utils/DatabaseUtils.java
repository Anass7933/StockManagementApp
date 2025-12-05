package com.stockapp.utils;

import java.sql.*;

public class DatabaseUtils {
	private static final String HOST = "localhost";
	private static final String PORT = "5432";
	private static final String DATABASE = "stockdb";
	private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;
	private static final String USER = "user";
	private static final String PASSWORD = "user1";

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
