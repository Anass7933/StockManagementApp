package com.stockapp.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UserServiceImplTest {

	private static UserServiceImpl userService;
	private static User sharedUser;

	private static final String SHARED_USERNAME = "junit_shared_user";
	private static final UserRole SHARED_ROLE = UserRole.CASHIER;
	private static final String TEMP_USERNAME = "temp_delete_me";

	@BeforeAll
	static void setUp() {
		userService = new UserServiceImpl();
		System.out.println("--- Setup: Creating Shared User via Service ---");

		deleteIfExists(SHARED_USERNAME);
		deleteIfExists(TEMP_USERNAME);

		User newUser = new User(
				SHARED_USERNAME,
				"secret_hash",
				"Shared JUnit Cashier",
				SHARED_ROLE);

		sharedUser = userService.create(newUser);
		System.out.println("Shared User created with ID: " + sharedUser.getId());
	}

	private static void deleteIfExists(String username) {
		try {
			User oldUser = userService.findByUsername(username);
			if (oldUser != null) {
				userService.delete(oldUser.getId());
				System.out.println("Removed stale test user: " + username);
			}
		} catch (Exception e) {
			System.out.println("Warning: Cleanup failed for " + username + ", but proceeding...");
		}
	}

	@AfterAll
	static void tearDown() {
		System.out.println("--- Teardown: Deleting Shared User ---");
		deleteIfExists(SHARED_USERNAME);
		deleteIfExists(TEMP_USERNAME);
	}

	@Test
	void testRead_SharedUser() {
		System.out.println("Running: testRead_SharedUser");

		Optional<User> fetchedUser = assertDoesNotThrow(() -> {
			return userService.read(sharedUser.getId());
		}, "Critical Failure : The read function threw an unexpected exception.");

		assertTrue(fetchedUser.isPresent(), "Should find the shared user");
		assertEquals(SHARED_USERNAME, fetchedUser.get().getUserName());
		assertEquals("Shared JUnit Cashier", fetchedUser.get().getFullName());
		assertEquals(SHARED_ROLE, fetchedUser.get().getRole());
	}

	@Test
	void testFindByUsername() {
		System.out.println("Running: testFindByUsername");

		User foundUser = assertDoesNotThrow(() -> {
			return userService.findByUsername(SHARED_USERNAME);
		}, "Critical Failure: The findByUsername function threw an unexpected exception!");

		assertNotNull(foundUser, "Should return a user object");
		assertEquals(SHARED_USERNAME, foundUser.getUserName());
	}

	@Test
	void testUpdateUser_Success() {
		System.out.println("Running: testUpdateUser_Success");

		User currentUser = userService.read(sharedUser.getId()).orElseThrow();
		String originalName = currentUser.getFullName();
		String newName = originalName + "_Updated";

		currentUser.setFullname(newName);

		assertDoesNotThrow(() -> userService.update(currentUser),
				"Critical Failure : The update function threw an unexpected exception!");

		User updatedUser = userService.read(sharedUser.getId()).orElseThrow();
		assertEquals(newName, updatedUser.getFullName());

		try {
			System.out.println("	undoing changes for other services");
			currentUser.setFullname(originalName);
			userService.update(currentUser);
		} catch (Exception e) {
			throw new RuntimeException("	Error undoing changes", e);
		}

	}

	@Test
	void testFindByRole() {
		System.out.println("Running: testFindByRole");

		List<User> cashiers = assertDoesNotThrow(() -> {
			return userService.findByRole(SHARED_ROLE);
		}, "Critical Failure : The findByRole function threw an unexpected exception!");

		boolean found = cashiers.stream()
				.anyMatch(u -> u.getUserName().equals(SHARED_USERNAME));

		assertTrue(found, "The list of ADMINs should contain our shared test user");
	}

	@Test
	void testDeleteUser_Independent() {
		System.out.println("Running: testDeleteUser_Independent");

		User tempUser = new User("temp_delete_me", "pass", "Disposable", UserRole.CASHIER);
		User createdTemp = userService.create(tempUser);

		assertNotNull(createdTemp.getId());

		assertDoesNotThrow(() -> userService.delete(tempUser.getId()),
				"Critical Failure : The delete function threw an unexpected exception!");

		Optional<User> deleted = userService.read(createdTemp.getId());
		assertFalse(deleted.isPresent(), "User should be gone");
	}
}
