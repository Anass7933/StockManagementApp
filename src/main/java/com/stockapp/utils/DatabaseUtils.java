package com.stockapp.utils;

import java.sql.*;

public class DatabaseUtils {

    private static final String HOST = "localhost";
    private static final String PORT = "5432";
    private static final String DATABASE = "mydb";
    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;

    private static final String USER = "postgres";
    private static final String PASSWORD = "your_password";

    private static final String DRIVER = "org.postgresql.Driver";

    static {
        try {
            Class.forName(DRIVER);
            System.out.println("PostgreSQL driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL driver not found!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Connected to PostgreSQL database: " + DATABASE);
            System.out.println("PostgreSQL version: " +
                    conn.getMetaData().getDatabaseProductVersion());
            return true;
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
}