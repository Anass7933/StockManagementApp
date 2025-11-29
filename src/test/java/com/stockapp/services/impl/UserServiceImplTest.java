package com.stockapp.services.impl;

import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {

	private UserServiceImpl userService;
	private final String TEST_USERNAME = "junit_test_user";

	@BeforeEach
	void setUp() throws SQLException {
		userService = new UserServiceImpl();
		// Ensure clean state before every test
		deleteTestUser();
	}

	@AfterEach
	void tearDown() throws SQLException {
		// Always clean up the database after every test
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

		// 1. Create a User Object
		// Note: ID and CreatedAt are null initially, set by DB
		User newUser = new User(
				TEST_USERNAME,
				"hashed_secret_123",
				"JUnit Test Admin",
				UserRole.ADMIN, // Ensure this matches an enum value in your UserRole.java
				null);

		// 2. Save to DB
		User createdUser = userService.create(newUser);

		// 3. Verify Creation
		assertNotNull(createdUser.getId(), "User ID should be generated");
		assertNotNull(createdUser.getCreatedAt(), "Created At should be generated");
		assertEquals(TEST_USERNAME, createdUser.getUserName());
		assertEquals(UserRole.ADMIN, createdUser.getRole());

		// 4. Read back from DB using ID
		Optional<User> fetchedUser = userService.read(createdUser.getId());

		assertTrue(fetchedUser.isPresent(), "Should be able to find user by ID");
		assertEquals("JUnit Test Admin", fetchedUser.get().getFullName());
	}

	@Test
	void testUpdateUser_Success() {
		System.out.println("Running: testUpdateUser_Success");

		// 1. Create initial user
		User user = userService.create(new User(
				TEST_USERNAME, "pass1", "Original Name", UserRole.CASHIER, null));

		// 2. Modify object
		// We need to create a new object or modify the existing one with the ID
		User userToUpdate = new User(
				user.getId(), // Important: keep the ID
				TEST_USERNAME,
				"pass2_updated",
				"Updated Name",
				UserRole.STOCK_MANAGER,
				user.getCreatedAt());

		// 3. Perform Update
		userService.update(userToUpdate);

		// 4. Read back and Verify
		User updatedUser = userService.read(user.getId()).orElseThrow();

		assertEquals("Updated Name", updatedUser.getFullName());
		assertEquals("pass2_updated", updatedUser.getPasswordHash());
		assertEquals(UserRole.STOCK_MANAGER, updatedUser.getRole());
	}

	@Test
	void testDeleteUser() {
		System.out.println("Running: testDeleteUser");

		// 1. Create user
		User user = userService.create(new User(
				TEST_USERNAME, "pass", "To Delete", UserRole.CASHIER, null));

		// 2. Delete user
		userService.delete(user.getId());

		// 3. Verify deletion
		Optional<User> deletedUser = userService.read(user.getId());
		assertFalse(deletedUser.isPresent(), "User should not exist after deletion");
	}

	@Test
	void testFindByUsername() {
		System.out.println("Running: testFindByUsername");

		// 1. Create user
		userService.create(new User(
				TEST_USERNAME, "pass", "Find Me", UserRole.ADMIN, null));

		// 2. Find by username
		User foundUser = userService.findByUsername(TEST_USERNAME);

		// 3. Verify
		assertNotNull(foundUser, "Should find user by username");
		assertEquals(TEST_USERNAME, foundUser.getUserName());
	}

	@Test
	void testFindByRole() {
		System.out.println("Running: testFindByRole");

		// 1. Create user with specific role
		userService.create(new User(
				TEST_USERNAME, "pass", "Role Tester", UserRole.CASHIER, null));

		// 2. Search for that role
		List<User> cashiers = userService.findByRole(UserRole.CASHIER);

		// 3. Verify at least our test user is there
		// Note: There might be other cashiers in the DB, so we check if ours is in the
		// list
		boolean found = cashiers.stream()
				.anyMatch(u -> u.getUserName().equals(TEST_USERNAME));

		assertTrue(found, "List of CASHIERs should contain the test user");
	}
}
