package com.stockapp.utils;

import org.junit.jupiter.api.Test;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;


public class DatabaseUtilsTest {

    @Test
    public void testGetConnection_ShouldReturnValidConnection() throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseUtils.getConnection();

            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");
        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    @Test
    public void testGetConnection_ShouldBeAbleToExecuteQuery() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtils.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT 1 as test_value");

            assertTrue(resultSet.next(), "Query should return a result");
            assertEquals(1, resultSet.getInt("test_value"), "Query should return correct value");
        } finally {
            DatabaseUtils.closeResources(connection, statement, resultSet);
        }
    }

    @Test
    public void testCloseResources_ShouldCloseAllResources() throws SQLException {
        Connection connection = DatabaseUtils.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT 1");

        DatabaseUtils.closeResources(connection, statement, resultSet);

        assertTrue(resultSet.isClosed(), "ResultSet should be closed");
        assertTrue(statement.isClosed(), "Statement should be closed");
        assertTrue(connection.isClosed(), "Connection should be closed");
    }

    @Test
    public void testCloseResources_ShouldHandleNullConnection() {
        // Should not throw exception when closing null resources
        assertDoesNotThrow(() -> DatabaseUtils.closeResources(null, null, null),
                "Closing null resources should not throw exception");
    }

    @Test
    public void testCloseResources_ShouldHandlePartiallyNullResources() throws SQLException {
        Connection connection = DatabaseUtils.getConnection();

        assertDoesNotThrow(() -> DatabaseUtils.closeResources(connection, null, null),
                "Should handle null statement and resultset");

        assertTrue(connection.isClosed(), "Connection should still be closed");
    }

    @Test
    public void testGetConnection_MultipleConnections() throws SQLException {
        Connection conn1 = null;
        Connection conn2 = null;

        try {
            conn1 = DatabaseUtils.getConnection();
            conn2 = DatabaseUtils.getConnection();

            assertNotNull(conn1);
            assertNotNull(conn2);
            assertNotSame(conn1, conn2, "Should create separate connection instances");
        } finally {
            if (conn1 != null)
                conn1.close();
            if (conn2 != null)
                conn2.close();
        }
    }

    @Test
    public void testGetConnection_ShouldConnectToCorrectDatabase() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtils.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT current_database()");

            assertTrue(resultSet.next());
            String dbName = resultSet.getString(1);
            assertEquals("stockdb", dbName, "Should connect to stockdb database");
        } finally {
            DatabaseUtils.closeResources(connection, statement, resultSet);
        }
    }

    @Test
    public void testCloseResources_ShouldHandleAlreadyClosedResources() throws SQLException {
        Connection connection = DatabaseUtils.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT 1");

        // Close resources manually first
        resultSet.close();
        statement.close();
        connection.close();

        // Should not throw exception when closing already closed resources
        assertDoesNotThrow(() -> DatabaseUtils.closeResources(connection, statement, resultSet),
                "Should handle already closed resources gracefully");
    }
}
