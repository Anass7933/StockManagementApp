package com.stockapp.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.utils.DatabaseUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceImplTest {
	private UserServiceImpl userService;
	private final String TEST_USERNAME = "junit_test_user";

	@BeforeEach
	void setUp() throws SQLException {
		userService = new UserServiceImpl();
		deleteTestUser();
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
	void testCreateAndReadUser() {
		System.out.println("Running: testCreateAndReadUser");
		User newUser = new User(TEST_USERNAME, "hashed_secret_123", "JUnit Test Admin", UserRole.ADMIN, null);
		User createdUser = userService.create(newUser);
		assertNotNull(createdUser.getId(), "User ID should be generated");
		assertNotNull(createdUser.getCreatedAt(), "Created At should be generated");
		assertEquals(TEST_USERNAME, createdUser.getUserName());
		assertEquals(UserRole.ADMIN, createdUser.getRole());
		Optional<User> fetchedUser = userService.read(createdUser.getId());
		assertTrue(fetchedUser.isPresent(), "Should be able to find user by ID");
		assertEquals("JUnit Test Admin", fetchedUser.get().getFullName());
	}

	@Test
	void testUpdateUser_Success() {
		System.out.println("Running: testUpdateUser_Success");
		User user = userService.create(new User(TEST_USERNAME, "pass1", "Original Name", UserRole.CASHIER, null));
		User userToUpdate = new User(
				user.getId(), TEST_USERNAME, "pass2_updated", "Updated Name", UserRole.STOCK_MANAGER,
				user.getCreatedAt());
		userService.update(userToUpdate);
		User updatedUser = userService.read(user.getId()).orElseThrow();
		assertEquals("Updated Name", updatedUser.getFullName());
		assertEquals("pass2_updated", updatedUser.getPasswordHash());
		assertEquals(UserRole.STOCK_MANAGER, updatedUser.getRole());
	}

	@Test
	void testDeleteUser() {
		System.out.println("Running: testDeleteUser");
		User user = userService.create(new User(TEST_USERNAME, "pass", "To Delete", UserRole.CASHIER, null));
		userService.delete(user.getId());
		Optional<User> deletedUser = userService.read(user.getId());
		assertFalse(deletedUser.isPresent(), "User should not exist after deletion");
	}

	@Test
	void testFindByUsername() {
		System.out.println("Running: testFindByUsername");
		userService.create(new User(TEST_USERNAME, "pass", "Find Me", UserRole.ADMIN, null));
		User foundUser = userService.findByUsername(TEST_USERNAME);
		assertNotNull(foundUser, "Should find user by username");
		assertEquals(TEST_USERNAME, foundUser.getUserName());
	}

	@Test
	void testFindByRole() {
		System.out.println("Running: testFindByRole");
		userService.create(new User(TEST_USERNAME, "pass", "Role Tester", UserRole.CASHIER, null));
		List<User> cashiers = userService.findByRole(UserRole.CASHIER);
		boolean found = cashiers.stream().anyMatch(u -> u.getUserName().equals(TEST_USERNAME));
		assertTrue(found, "List of CASHIERs should contain the test user");
	}
}
