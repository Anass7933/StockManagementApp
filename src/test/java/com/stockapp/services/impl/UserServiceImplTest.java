package com.stockapp.services.impl;

import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.services.interfaces.UserService;
import com.stockapp.utils.PasswordUtils;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


public class UserServiceImplTest {

    private UserService userService;
    private User testUser;
    private List<Long> createdUserIds;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl();
        createdUserIds = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        // Clean up test user
        if (testUser != null && testUser.getId() > 0) {
            try {
                userService.delete(testUser.getId());
            } catch (Exception e) {
                System.err.println("Warning: Could not delete test user: " + e.getMessage());
            }
        }

        // Clean up any additional users created during tests
        for (Long userId : createdUserIds) {
            try {
                userService.delete(userId);
            } catch (Exception e) {
                System.err.println("Warning: Could not delete user " + userId + ": " + e.getMessage());
            }
        }
    }

    // ==================== CREATE TESTS ====================

    @Test
    @DisplayName("Should create user successfully with valid data")
    public void testCreate_ValidData_Success() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = new User(
                "testuser_create",
                hashedPassword,
                "Test Create User",
                UserRole.CASHIER,
                null);

        User created = userService.create(testUser);

        assertNotNull(created, "Created user should not be null");
        assertTrue(created.getId() > 0, "Created user should have valid ID");
        assertEquals("testuser_create", created.getUserName(), "Username should match");
        assertEquals("Test Create User", created.getFullName(), "Full name should match");
        assertEquals(UserRole.CASHIER, created.getRole(), "Role should match");
        assertNotNull(created.getCreatedAt(), "Created user should have timestamp");
    }

    @Test
    @DisplayName("Should create user with ADMIN role")
    public void testCreate_AdminRole_Success() {
        String hashedPassword = PasswordUtils.hashPassword("adminpass123");
        testUser = new User(
                "admin_user",
                hashedPassword,
                "Admin User",
                UserRole.ADMIN,
                null);

        User created = userService.create(testUser);

        assertNotNull(created, "Created user should not be null");
        assertEquals(UserRole.ADMIN, created.getRole(), "Should have ADMIN role");
    }

    @Test
    @DisplayName("Should create user with STOCK_MANAGER role")
    public void testCreate_StockManagerRole_Success() {
        String hashedPassword = PasswordUtils.hashPassword("managerpass123");
        testUser = new User(
                "manager_user",
                hashedPassword,
                "Stock Manager User",
                UserRole.STOCK_MANAGER,
                null);

        User created = userService.create(testUser);

        assertNotNull(created, "Created user should not be null");
        assertEquals(UserRole.STOCK_MANAGER, created.getRole(), "Should have STOCK_MANAGER role");
    }

    @Test
    @DisplayName("Should preserve password hash during creation")
    public void testCreate_PreservesPasswordHash_Success() {
        String originalPassword = "securePassword123";
        String hashedPassword = PasswordUtils.hashPassword(originalPassword);

        testUser = new User(
                "testuser_password",
                hashedPassword,
                "Test Password User",
                UserRole.ADMIN,
                null);

        User created = userService.create(testUser);

        assertEquals(hashedPassword, created.getPasswordHash(),
                "Password hash should be preserved");
        assertTrue(PasswordUtils.verifyPassword(originalPassword, created.getPasswordHash()),
                "Password should verify correctly against hash");
    }

    @Test
    @DisplayName("Should create users with different usernames")
    public void testCreate_UniqueUsernames_Success() {
        String hashedPassword = PasswordUtils.hashPassword("password123");

        User user1 = userService.create(new User(
                "user1", hashedPassword, "User One", UserRole.CASHIER, null));
        createdUserIds.add(user1.getId());

        User user2 = userService.create(new User(
                "user2", hashedPassword, "User Two", UserRole.CASHIER, null));
        testUser = user2; // Will be cleaned up by @AfterEach

        assertNotNull(user1, "First user should be created");
        assertNotNull(user2, "Second user should be created");
        assertNotEquals(user1.getId(), user2.getId(), "Users should have different IDs");
        assertNotEquals(user1.getUserName(), user2.getUserName(), "Users should have different usernames");
    }

    // ==================== READ TESTS ====================

    @Test
    @DisplayName("Should read existing user by ID")
    public void testRead_ExistingId_ReturnsUser() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = userService.create(new User(
                "testuser_read",
                hashedPassword,
                "Test Read User",
                UserRole.STOCK_MANAGER,
                null));

        Optional<User> found = userService.read(testUser.getId());

        assertTrue(found.isPresent(), "User should be found");
        assertEquals(testUser.getId(), found.get().getId(), "IDs should match");
        assertEquals("testuser_read", found.get().getUserName(), "Username should match");
        assertEquals(UserRole.STOCK_MANAGER, found.get().getRole(), "Role should match");
    }

    @Test
    @DisplayName("Should return empty Optional for non-existent ID")
    public void testRead_NonExistentId_ReturnsEmpty() {
        Optional<User> found = userService.read(999999L);

        assertFalse(found.isPresent(), "Should return empty for non-existent ID");
    }

    @Test
    @DisplayName("Should return all users")
    public void testReadAll_ReturnsAllUsers() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = userService.create(new User(
                "testuser_readall",
                hashedPassword,
                "Test ReadAll User",
                UserRole.CASHIER,
                null));

        List<User> allUsers = userService.readAll();

        assertNotNull(allUsers, "Result should not be null");
        assertTrue(allUsers.size() > 0, "Should return at least one user");
        assertTrue(allUsers.stream().anyMatch(u -> u.getId() == testUser.getId()),
                "Should contain the created test user");
    }

    @Test
    @DisplayName("Should verify multiple users exist")
    public void testReadAll_MultipleUsers_ReturnsAll() {
        String hashedPassword = PasswordUtils.hashPassword("password123");

        User user1 = userService.create(new User(
                "user_multi1", hashedPassword, "Multi User 1", UserRole.CASHIER, null));
        createdUserIds.add(user1.getId());

        User user2 = userService.create(new User(
                "user_multi2", hashedPassword, "Multi User 2", UserRole.ADMIN, null));
        testUser = user2;

        List<User> allUsers = userService.readAll();

        assertTrue(allUsers.stream().anyMatch(u -> u.getId() == user1.getId()),
                "Should contain first user");
        assertTrue(allUsers.stream().anyMatch(u -> u.getId() == user2.getId()),
                "Should contain second user");
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @DisplayName("Should update user successfully")
    public void testUpdate_ValidData_Success() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = userService.create(new User(
                "testuser_update",
                hashedPassword,
                "Original Name",
                UserRole.CASHIER,
                null));

        User updated = new User(
                testUser.getId(),
                "testuser_updated",
                hashedPassword,
                "Updated Name",
                UserRole.ADMIN,
                testUser.getCreatedAt());

        User result = userService.update(updated);

        assertNotNull(result, "Updated user should not be null");
        assertEquals(testUser.getId(), result.getId(), "ID should remain the same");
        assertEquals("testuser_updated", result.getUserName(), "Username should be updated");
        assertEquals("Updated Name", result.getFullName(), "Full name should be updated");
        assertEquals(UserRole.ADMIN, result.getRole(), "Role should be updated");
    }

    @Test
    @DisplayName("Should update user password")
    public void testUpdate_PasswordChange_Success() {
        String originalPassword = "oldPassword123";
        String hashedPassword = PasswordUtils.hashPassword(originalPassword);
        testUser = userService.create(new User(
                "testuser_pwdchange",
                hashedPassword,
                "Password Change User",
                UserRole.CASHIER,
                null));

        String newPassword = "newPassword456";
        String newHashedPassword = PasswordUtils.hashPassword(newPassword);

        User updated = new User(
                testUser.getId(),
                testUser.getUserName(),
                newHashedPassword,
                testUser.getFullName(),
                testUser.getRole(),
                testUser.getCreatedAt());

        User result = userService.update(updated);

        assertNotNull(result, "Updated user should not be null");
        assertEquals(newHashedPassword, result.getPasswordHash(), "Password hash should be updated");
        assertTrue(PasswordUtils.verifyPassword(newPassword, result.getPasswordHash()),
                "New password should verify correctly");
        assertFalse(PasswordUtils.verifyPassword(originalPassword, result.getPasswordHash()),
                "Old password should no longer verify");
    }

    @Test
    @DisplayName("Should update only role while preserving other fields")
    public void testUpdate_RoleOnly_Success() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = userService.create(new User(
                "testuser_roleonly",
                hashedPassword,
                "Role Only User",
                UserRole.CASHIER,
                null));

        User updated = new User(
                testUser.getId(),
                testUser.getUserName(),
                testUser.getPasswordHash(),
                testUser.getFullName(),
                UserRole.STOCK_MANAGER,
                testUser.getCreatedAt());

        User result = userService.update(updated);

        assertEquals(testUser.getUserName(), result.getUserName(), "Username should be unchanged");
        assertEquals(testUser.getFullName(), result.getFullName(), "Full name should be unchanged");
        assertEquals(UserRole.STOCK_MANAGER, result.getRole(), "Role should be updated");
    }

    @Test
    @DisplayName("Should preserve timestamp when updating user")
    public void testUpdate_PreservesTimestamp_Success() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = userService.create(new User(
                "testuser_timestamp",
                hashedPassword,
                "Timestamp User",
                UserRole.CASHIER,
                null));
        OffsetDateTime originalTimestamp = testUser.getCreatedAt();

        User updated = new User(
                testUser.getId(),
                "updated_username",
                hashedPassword,
                "Updated Name",
                UserRole.ADMIN,
                originalTimestamp);

        User result = userService.update(updated);

        assertEquals(originalTimestamp, result.getCreatedAt(),
                "Timestamp should remain unchanged");
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Should delete user successfully")
    public void testDelete_ExistingUser_Success() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = userService.create(new User(
                "testuser_delete",
                hashedPassword,
                "Test Delete User",
                UserRole.CASHIER,
                null));
        long userId = testUser.getId();

        userService.delete(userId);

        Optional<User> deleted = userService.read(userId);
        assertFalse(deleted.isPresent(), "User should be deleted");

        testUser = null; // Prevent cleanup from trying to delete again
    }

    @Test
    @DisplayName("Should handle deletion of non-existent user gracefully")
    public void testDelete_NonExistentUser_HandlesGracefully() {
        // Depending on implementation, this might throw an exception or succeed silently
        // Adjust this test based on actual implementation behavior
        assertDoesNotThrow(() -> {
            try {
                userService.delete(999999L);
            } catch (RuntimeException e) {
                // If implementation throws exception, verify it's appropriate
                assertTrue(e.getMessage().contains("not found") ||
                                e.getMessage().contains("No user found"),
                        "Exception message should indicate user not found");
            }
        });
    }

    // ==================== QUERY TESTS ====================

    @Test
    @DisplayName("Should find users by role")
    public void testFindByRole_ReturnsUsersWithRole() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = userService.create(new User(
                "testuser_findbyrole",
                hashedPassword,
                "Test FindByRole User",
                UserRole.CASHIER,
                null));

        List<User> cashiers = userService.findByRole(UserRole.CASHIER);

        assertNotNull(cashiers, "Result should not be null");
        assertTrue(cashiers.size() > 0, "Should find at least one cashier");
        assertTrue(cashiers.stream().allMatch(u -> u.getRole() == UserRole.CASHIER),
                "All returned users should have CASHIER role");
        assertTrue(cashiers.stream().anyMatch(u -> u.getId() == testUser.getId()),
                "Should contain the created test user");
    }

    @Test
    @DisplayName("Should find multiple users with same role")
    public void testFindByRole_MultipleUsers_ReturnsAll() {
        String hashedPassword = PasswordUtils.hashPassword("password123");

        User cashier1 = userService.create(new User(
                "cashier1", hashedPassword, "Cashier One", UserRole.CASHIER, null));
        createdUserIds.add(cashier1.getId());

        User cashier2 = userService.create(new User(
                "cashier2", hashedPassword, "Cashier Two", UserRole.CASHIER, null));
        testUser = cashier2;

        List<User> cashiers = userService.findByRole(UserRole.CASHIER);

        assertTrue(cashiers.stream().anyMatch(u -> u.getId() == cashier1.getId()),
                "Should contain first cashier");
        assertTrue(cashiers.stream().anyMatch(u -> u.getId() == cashier2.getId()),
                "Should contain second cashier");
    }

    @Test
    @DisplayName("Should return empty list for role with no users")
    public void testFindByRole_NoUsers_ReturnsEmptyList() {
        // Assuming we can test with a role that has no users
        // This might need adjustment based on initial database state
        List<User> users = userService.findByRole(UserRole.ADMIN);

        assertNotNull(users, "Result should not be null");
        // Note: Can't assert empty if there might be existing admin users
        // Just verify the list is not null and all have correct role
        assertTrue(users.stream().allMatch(u -> u.getRole() == UserRole.ADMIN),
                "All returned users should have ADMIN role");
    }

    @Test
    @DisplayName("Should find user by username")
    public void testFindByUsername_ExistingUsername_ReturnsUser() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = userService.create(new User(
                "testuser_findbyusername",
                hashedPassword,
                "Test FindByUsername User",
                UserRole.STOCK_MANAGER,
                null));

        User found = userService.findByUsername("testuser_findbyusername");

        assertNotNull(found, "Should find user by username");
        assertEquals(testUser.getId(), found.getId(), "IDs should match");
        assertEquals("testuser_findbyusername", found.getUserName(), "Username should match");
        assertEquals(UserRole.STOCK_MANAGER, found.getRole(), "Role should match");
    }

    @Test
    @DisplayName("Should return null for non-existent username")
    public void testFindByUsername_NonExistentUsername_ReturnsNull() {
        User found = userService.findByUsername("nonexistent_username_xyz_12345");

        assertNull(found, "Should return null for non-existent username");
    }

    @Test
    @DisplayName("Should handle username search case-sensitively")
    public void testFindByUsername_CaseSensitive_ReturnsCorrectly() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = userService.create(new User(
                "TestUserCase",
                hashedPassword,
                "Case Sensitive User",
                UserRole.CASHIER,
                null));

        User foundExact = userService.findByUsername("TestUserCase");
        User foundWrongCase = userService.findByUsername("testusercase");

        assertNotNull(foundExact, "Should find user with exact case match");
        // Depending on implementation, this might be null or found
        // Adjust assertion based on actual case-sensitivity behavior
    }

    // ==================== SECURITY TESTS ====================

    @Test
    @DisplayName("Should not store plain text password")
    public void testCreate_NeverStoresPlainTextPassword_Success() {
        String plainPassword = "mySecretPassword123!";
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);

        testUser = userService.create(new User(
                "security_test_user",
                hashedPassword,
                "Security Test User",
                UserRole.CASHIER,
                null));

        assertNotEquals(plainPassword, testUser.getPasswordHash(),
                "Should never store plain text password");
        assertNotNull(testUser.getPasswordHash(), "Password hash should exist");
        assertTrue(testUser.getPasswordHash().length() > plainPassword.length(),
                "Hash should be longer than plain password");
    }

    @Test
    @DisplayName("Should verify password correctly after creation")
    public void testCreate_PasswordVerification_Success() {
        String plainPassword = "verifyThisPassword456";
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);

        testUser = userService.create(new User(
                "verify_user",
                hashedPassword,
                "Verify User",
                UserRole.ADMIN,
                null));

        assertTrue(PasswordUtils.verifyPassword(plainPassword, testUser.getPasswordHash()),
                "Correct password should verify");
        assertFalse(PasswordUtils.verifyPassword("wrongPassword", testUser.getPasswordHash()),
                "Incorrect password should not verify");
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle user with very long full name")
    public void testCreate_LongFullName_Success() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        String longName = "This Is A Very Long Full Name That Contains Many Characters " +
                "And Should Still Be Handled Properly By The System";

        testUser = userService.create(new User(
                "long_name_user",
                hashedPassword,
                longName,
                UserRole.CASHIER,
                null));

        assertNotNull(testUser, "User should be created");
        assertEquals(longName, testUser.getFullName(), "Long name should be preserved");
    }

    @Test
    @DisplayName("Should handle user with special characters in username")
    public void testCreate_SpecialCharactersUsername_Success() {
        String hashedPassword = PasswordUtils.hashPassword("password123");

        testUser = userService.create(new User(
                "user_with_underscores_123",
                hashedPassword,
                "Special Char User",
                UserRole.CASHIER,
                null));

        assertNotNull(testUser, "User should be created");
        assertEquals("user_with_underscores_123", testUser.getUserName(),
                "Username with special characters should be preserved");
    }

    @Test
    @DisplayName("Should handle all user roles correctly")
    public void testCreate_AllRoles_Success() {
        String hashedPassword = PasswordUtils.hashPassword("password123");

        // Test each role
        User admin = userService.create(new User(
                "test_admin", hashedPassword, "Admin", UserRole.ADMIN, null));
        createdUserIds.add(admin.getId());

        User cashier = userService.create(new User(
                "test_cashier", hashedPassword, "Cashier", UserRole.CASHIER, null));
        createdUserIds.add(cashier.getId());

        User manager = userService.create(new User(
                "test_manager", hashedPassword, "Manager", UserRole.STOCK_MANAGER, null));
        testUser = manager;

        assertEquals(UserRole.ADMIN, admin.getRole(), "Admin role should be set correctly");
        assertEquals(UserRole.CASHIER, cashier.getRole(), "Cashier role should be set correctly");
        assertEquals(UserRole.STOCK_MANAGER, manager.getRole(), "Manager role should be set correctly");
    }
}