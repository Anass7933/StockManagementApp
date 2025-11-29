package com.stockapp.utils;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

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
        
        // 1. Create real resources
        Connection conn = DatabaseUtils.getConnection();
        Statement stmt = conn.createStatement();
        // Just a simple query to get a ResultSet
        ResultSet rs = stmt.executeQuery("SELECT 1"); 

        // Verify they are open initially
        assertFalse(conn.isClosed());
        assertFalse(stmt.isClosed());
        assertFalse(rs.isClosed());

        // 2. Close them using your utility
        DatabaseUtils.closeResources(conn, stmt, rs);

        // 3. Verify they are closed
        // Note: Closing connection usually closes stmt and rs automatically, 
        // but your utility explicitly closes them, which is safer.
        assertTrue(rs.isClosed(), "ResultSet should be closed");
        assertTrue(stmt.isClosed(), "Statement should be closed");
        assertTrue(conn.isClosed(), "Connection should be closed");
    }

    @Test
    void testCloseResources_NullSafe() {
        System.out.println("Running: testCloseResources_NullSafe");
        
        // This should run without throwing a NullPointerException
        assertDoesNotThrow(() -> {
            DatabaseUtils.closeResources(null, null, null);
        }, "Should handle null resources gracefully");
    }

    @Test
    void testCloseResources_MixedNulls() throws SQLException {
        System.out.println("Running: testCloseResources_MixedNulls");
        
        Connection conn = DatabaseUtils.getConnection();
        
        // Pass nulls for Statement and ResultSet, but a real Connection
        assertDoesNotThrow(() -> {
            DatabaseUtils.closeResources(conn, null, null);
        });
        
        assertTrue(conn.isClosed(), "Connection should still be closed even if others were null");
    }
}
