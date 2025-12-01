package com.stockapp.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.utils.DatabaseUtils;
import com.stockapp.utils.PasswordUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceImplTest {
	private AuthServiceImpl authService;
	private final String TEST_USERNAME = "junit_integr_test_user";
	private final String TEST_PASSWORD_RAW = "secureTestPass123";
	private final String TEST_FULLNAME = "JUnit Integration Agent";
	private final UserRole TEST_ROLE = UserRole.CASHIER;

	@BeforeEach
	void setUp() throws SQLException {
		authService = new AuthServiceImpl();
		deleteTestUser();
		String hashedPassword = PasswordUtils.hashPassword(TEST_PASSWORD_RAW);
		try (Connection conn = DatabaseUtils.getConnection()) {
			String sql = "INSERT INTO users (username, password_hash, full_name, role, created_at) "
					+ "VALUES (?, ?, ?, ?::user_role, NOW())";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, TEST_USERNAME);
				ps.setString(2, hashedPassword);
				ps.setString(3, TEST_FULLNAME);
				ps.setString(4, TEST_ROLE.name());
				ps.executeUpdate();
			}
		}
	}

	@AfterEach
	void tearDown() throws SQLException {
		deleteTestUser();
	}

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
		User user = authService.validateLogin(TEST_USERNAME, TEST_PASSWORD_RAW);
		assertNotNull(user, "Login should return a User object for valid credentials");
		assertEquals(TEST_USERNAME, user.getUserName(), "Username should match");
		assertEquals(TEST_ROLE, user.getRole(), "Role should be CASHIER");
		assertEquals(TEST_FULLNAME, user.getFullName(), "Full Name should match");
		assertNotNull(user.getId(), "User ID should be populated from DB");
	}

	@Test
	void testValidateLogin_WrongPassword() {
		System.out.println("Running: testValidateLogin_WrongPassword");
		User user = authService.validateLogin(TEST_USERNAME, "WrongPassword123");
		assertNull(user, "Login should return null when password is incorrect");
	}

	@Test
	void testValidateLogin_UserNotFound() {
		System.out.println("Running: testValidateLogin_UserNotFound");
		User user = authService.validateLogin("ghost_user_9999", "anyPassword");
		assertNull(user, "Login should return null for non-existent username");
	}
}
