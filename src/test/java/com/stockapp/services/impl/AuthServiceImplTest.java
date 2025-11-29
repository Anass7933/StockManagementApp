package com.stockapp.services.impl;

import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.utils.DatabaseUtils;
import com.stockapp.utils.PasswordUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test for AuthServiceImpl.
 * * NOTE: This test connects to the REAL 'stockdb' database.
 * It creates a temporary user before every test and deletes it afterwards.
 */
class AuthServiceImplTest {

	private AuthServiceImpl authService;

	// Test Data Constants
	private final String TEST_USERNAME = "junit_integr_test_user";
	private final String TEST_PASSWORD_RAW = "secureTestPass123";
	private final String TEST_FULLNAME = "JUnit Integration Agent";
	private final UserRole TEST_ROLE = UserRole.CASHIER;

	@BeforeEach
	void setUp() throws SQLException {
		authService = new AuthServiceImpl();

		// 1. Clean up any leftovers from previous failed runs to avoid "Duplicate Key"
		// errors
		deleteTestUser();

		// 2. Hash the raw password using YOUR real util
		String hashedPassword = PasswordUtils.hashPassword(TEST_PASSWORD_RAW);

		// 3. Insert the test user into the real DB
		try (Connection conn = DatabaseUtils.getConnection()) {
			// Note: We use 'NOW()' for created_at. Adjust SQL if your DB requires specific
			// time format.
			String sql = "INSERT INTO users (username, password_hash, full_name, role, created_at) " +
					"VALUES (?, ?, ?, ?::user_role, NOW())";

			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, TEST_USERNAME);
				ps.setString(2, hashedPassword);
				ps.setString(3, TEST_FULLNAME);
				ps.setString(4, TEST_ROLE.name()); // Converts Enum CASHIER -> "CASHIER"
				ps.executeUpdate();
			}
		}
	}

	@AfterEach
	void tearDown() throws SQLException {
		// 4. Always clean up after the test finishes
		deleteTestUser();
	}

	// Helper method to delete the test user
	private void deleteTestUser() throws SQLException {
		try (Connection conn = DatabaseUtils.getConnection()) {
			String sql = "DELETE FROM users WHERE username = ?";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, TEST_USERNAME);
				ps.executeUpdate();
			}
		}
	}

	@Test
	void testValidateLogin_Success() {
		System.out.println("Running: testValidateLogin_Success");

		// Act
		User user = authService.validateLogin(TEST_USERNAME, TEST_PASSWORD_RAW);

		// Assert
		assertNotNull(user, "Login should return a User object for valid credentials");
		assertEquals(TEST_USERNAME, user.getUserName(), "Username should match");
		assertEquals(TEST_ROLE, user.getRole(), "Role should be CASHIER");
		assertEquals(TEST_FULLNAME, user.getFullName(), "Full Name should match");
		assertNotNull(user.getId(), "User ID should be populated from DB");
	}

	@Test
	void testValidateLogin_WrongPassword() {
		System.out.println("Running: testValidateLogin_WrongPassword");

		// Act
		User user = authService.validateLogin(TEST_USERNAME, "WrongPassword123");

		// Assert
		assertNull(user, "Login should return null when password is incorrect");
	}

	@Test
	void testValidateLogin_UserNotFound() {
		System.out.println("Running: testValidateLogin_UserNotFound");

		// Act
		User user = authService.validateLogin("ghost_user_9999", "anyPassword");

		// Assert
		assertNull(user, "Login should return null for non-existent username");
	}
}
