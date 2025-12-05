package com.stockapp.services.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.services.interfaces.UserService;
import com.stockapp.utils.PasswordUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AuthServiceImplTest {

	private static AuthServiceImpl authService;
	private static UserService userService;

	private static final String TEST_USERNAME = "junit_integr_test_user";
	private static final String TEST_PASSWORD_RAW = "secureTestPass123";
	private static final String TEST_FULLNAME = "JUnit Integration Agent";
	private static final UserRole TEST_ROLE = UserRole.CASHIER;

	@BeforeAll
	static void setUp() {
		authService = new AuthServiceImpl();
		userService = new UserServiceImpl();

		System.out.println("--- Setup: ensuring clean environment---");

		try {
			User oldUser = userService.findByUsername(TEST_USERNAME);
			if (oldUser != null) {
				userService.delete(oldUser.getId());
				System.out.println("Removed stale test user from previous crash.");
			}
		} catch (Exception e) {
			System.out.println("Warning: Cleanup failed, but proceeding...");
		}

		User newUser = new User(
				TEST_USERNAME,
				PasswordUtils.hashPassword(TEST_PASSWORD_RAW),
				TEST_FULLNAME,
				TEST_ROLE);
		userService.create(newUser);
		System.out.println("shared user created successfully with ID : " + newUser.getId());
	}

	@AfterAll
	static void tearDown() {
		System.out.println("--- Teardown: cleaning up ---");
		try {
			User userToDelete = userService.findByUsername(TEST_USERNAME);
			if (userToDelete != null) {
				userService.delete(userToDelete.getId());
				System.out.println("shared user deleted successfully.");
			}
		} catch (Exception e) {

			System.err.println("WARNING: Cleanup failed. Zombie data might remain.");
			e.printStackTrace();
		}
	}

	@Test
	void testValidateLogin_Success() {
		System.out.println("running : testValidateLogin_Success");

		User user = assertDoesNotThrow(() -> {
			return authService.validateLogin(TEST_USERNAME, TEST_PASSWORD_RAW);
		}, "Critical Failure : the validateLogin function threw an unexpected exception");

		assertNotNull(user, "Login should succeed");
		assertEquals(TEST_USERNAME, user.getUserName());
		assertEquals(TEST_ROLE, user.getRole());
	}

	@Test
	void testValidateLogin_WrongPassword() {
		System.out.println("running : testValidateLogin_WrongPassword");

		User user = assertDoesNotThrow(() -> {
			return authService.validateLogin(TEST_USERNAME, "WrongPassword123");
		}, "Critical Failure : the validateLogin function threw an unexpected exception");

		assertNull(user, "Login should fail with wrong password");
	}

	@Test
	void testValidateLogin_UserNotFound() {
		System.out.println("running : testValidateLogin_UserNotFound");
		String nonExistentUsername;
		int safetyCounter = 0;

		do {
			nonExistentUsername = "unknown_" + UUID.randomUUID().toString();

			if (safetyCounter++ > 20) {
				throw new RuntimeException(
						"Critical Test Failure: Unable to generate a unique non-existent username after 20 attempts.");
			}

		} while (userService.findByUsername(nonExistentUsername) != null);

		final String finalNonExistentUsername = nonExistentUsername;

		User user = assertDoesNotThrow(() -> {
			return authService.validateLogin(finalNonExistentUsername, TEST_PASSWORD_RAW);
		}, "Critical Failure : the validateLogin function threw an unexpected exception");

		assertNull(user, "Login should fail for unknown user");
	}
}
