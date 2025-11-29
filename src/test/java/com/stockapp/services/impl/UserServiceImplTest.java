package com.stockapp.services.impl;

import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.services.interfaces.UserService;
import com.stockapp.utils.PasswordUtils;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for UserServiceImpl
 * Tests CRUD operations without using Mockito
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceImplTest {

    private UserService userService;
    private User testUser;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl();
    }

    @AfterEach
    public void tearDown() {
        // Clean up test user
        if (testUser != null && testUser.getId() > 0) {
            try {
                userService.delete(testUser.getId());
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    @Order(1)
    public void testCreate_ShouldCreateUserSuccessfully() {
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = new User(
                "testuser_create",
                hashedPassword,
                "Test Create User",
                UserRole.CASHIER,
                null);

        User createdUser = userService.create(testUser);

        assertNotNull(createdUser);
        assertTrue(createdUser.getId() > 0, "Created user should have valid ID");
        assertEquals("testuser_create", createdUser.getUserName());
        assertEquals("Test Create User", createdUser.getFullName());
        assertEquals(UserRole.CASHIER, createdUser.getRole());
        assertNotNull(createdUser.getCreatedAt(), "Created user should have timestamp");
    }

    @Test
    @Order(2)
    public void testRead_ShouldReturnUserById() {
        // Create a user first
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = new User(
                "testuser_read",
                hashedPassword,
                "Test Read User",
                UserRole.STOCK_MANAGER,
                null);
        testUser = userService.create(testUser);

        // Read the user
        Optional<User> foundUser = userService.read(testUser.getId());

        assertTrue(foundUser.isPresent(), "User should be found");
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals("testuser_read", foundUser.get().getUserName());
        assertEquals(UserRole.STOCK_MANAGER, foundUser.get().getRole());
    }

    @Test
    @Order(3)
    public void testRead_ShouldReturnEmptyForNonExistentId() {
        Optional<User> foundUser = userService.read(999999L);

        assertFalse(foundUser.isPresent(), "Should return empty for non-existent ID");
    }

    @Test
    @Order(4)
    public void testUpdate_ShouldUpdateUserSuccessfully() {
        // Create a user first
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = new User(
                "testuser_update",
                hashedPassword,
                "Original Name",
                UserRole.CASHIER,
                null);
        testUser = userService.create(testUser);

        // Update the user
        User updatedUser = new User(
                testUser.getId(),
                "testuser_updated",
                hashedPassword,
                "Updated Name",
                UserRole.ADMIN,
                testUser.getCreatedAt());

        User result = userService.update(updatedUser);

        assertNotNull(result);
        assertEquals("testuser_updated", result.getUserName());
        assertEquals("Updated Name", result.getFullName());
        assertEquals(UserRole.ADMIN, result.getRole());
    }

    @Test
    @Order(5)
    public void testDelete_ShouldDeleteUserSuccessfully() {
        // Create a user first
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = new User(
                "testuser_delete",
                hashedPassword,
                "Test Delete User",
                UserRole.CASHIER,
                null);
        testUser = userService.create(testUser);
        long userId = testUser.getId();

        // Delete the user
        userService.delete(userId);

        // Verify deletion
        Optional<User> deletedUser = userService.read(userId);
        assertFalse(deletedUser.isPresent(), "User should be deleted");

        testUser = null; // Prevent cleanup from trying to delete again
    }

    @Test
    @Order(6)
    public void testReadAll_ShouldReturnAllUsers() {
        // Create test user
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = new User(
                "testuser_readall",
                hashedPassword,
                "Test ReadAll User",
                UserRole.CASHIER,
                null);
        testUser = userService.create(testUser);

        List<User> allUsers = userService.readAll();

        assertNotNull(allUsers);
        assertTrue(allUsers.size() > 0, "Should return at least one user");
        assertTrue(allUsers.stream().anyMatch(u -> u.getId() == testUser.getId()),
                "Should contain the created test user");
    }

    @Test
    @Order(7)
    public void testFindByRole_ShouldReturnUsersWithSpecificRole() {
        // Create test user with CASHIER role
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = new User(
                "testuser_findbyrole",
                hashedPassword,
                "Test FindByRole User",
                UserRole.CASHIER,
                null);
        testUser = userService.create(testUser);

        List<User> cashiers = userService.findByRole(UserRole.CASHIER);

        assertNotNull(cashiers);
        assertTrue(cashiers.size() > 0, "Should find at least one cashier");
        assertTrue(cashiers.stream().allMatch(u -> u.getRole() == UserRole.CASHIER),
                "All returned users should have CASHIER role");
        assertTrue(cashiers.stream().anyMatch(u -> u.getId() == testUser.getId()),
                "Should contain the created test user");
    }

    @Test
    @Order(8)
    public void testFindByUsername_ShouldReturnUserWithMatchingUsername() {
        // Create test user
        String hashedPassword = PasswordUtils.hashPassword("password123");
        testUser = new User(
                "testuser_findbyusername",
                hashedPassword,
                "Test FindByUsername User",
                UserRole.STOCK_MANAGER,
                null);
        testUser = userService.create(testUser);

        User foundUser = userService.findByUsername("testuser_findbyusername");

        assertNotNull(foundUser, "Should find user by username");
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals("testuser_findbyusername", foundUser.getUserName());
        assertEquals(UserRole.STOCK_MANAGER, foundUser.getRole());
    }

    @Test
    @Order(9)
    public void testFindByUsername_ShouldReturnNullForNonExistentUsername() {
        User foundUser = userService.findByUsername("nonexistent_username_xyz");

        assertNull(foundUser, "Should return null for non-existent username");
    }

    @Test
    @Order(10)
    public void testCreate_ShouldPreservePasswordHash() {
        String originalPassword = "securePassword123";
        String hashedPassword = PasswordUtils.hashPassword(originalPassword);

        testUser = new User(
                "testuser_password",
                hashedPassword,
                "Test Password User",
                UserRole.ADMIN,
                null);

        User createdUser = userService.create(testUser);

        assertEquals(hashedPassword, createdUser.getPasswordHash(),
                "Password hash should be preserved");
        assertTrue(PasswordUtils.verifyPassword(originalPassword, createdUser.getPasswordHash()),
                "Password should verify correctly");
    }
}
