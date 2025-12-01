package com.stockapp.utils;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;

class DatabaseUtilsTest {
	@Test
	void testGetConnection_Success() {
		System.out.println("Running: testGetConnection_Success");
		try (Connection conn = DatabaseUtils.getConnection()) {
			assertNotNull(conn, "Connection should not be null");
			assertFalse(conn.isClosed(), "Connection should be open");
			assertTrue(conn.isValid(2), "Connection should be valid (check timeout 2s)");
		} catch (SQLException e) {
			fail("Should successfully connect to the database: " + e.getMessage());
		}
	}

	@Test
	void testCloseResources_RealObjects() throws SQLException {
		System.out.println("Running: testCloseResources_RealObjects");
		Connection conn = DatabaseUtils.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT 1");
		assertFalse(conn.isClosed());
		assertFalse(stmt.isClosed());
		assertFalse(rs.isClosed());
		DatabaseUtils.closeResources(conn, stmt, rs);
		assertTrue(rs.isClosed(), "ResultSet should be closed");
		assertTrue(stmt.isClosed(), "Statement should be closed");
		assertTrue(conn.isClosed(), "Connection should be closed");
	}

	@Test
	void testCloseResources_NullSafe() {
		System.out.println("Running: testCloseResources_NullSafe");
		assertDoesNotThrow(
				() -> {
					DatabaseUtils.closeResources(null, null, null);
				}, "Should handle null resources gracefully");
	}

	@Test
	void testCloseResources_MixedNulls() throws SQLException {
		System.out.println("Running: testCloseResources_MixedNulls");
		Connection conn = DatabaseUtils.getConnection();
		assertDoesNotThrow(() -> {
			DatabaseUtils.closeResources(conn, null, null);
		});
		assertTrue(conn.isClosed(), "Connection should still be closed even if others were null");
	}
}
