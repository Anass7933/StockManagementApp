package com.stockapp.services.impl;

import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.services.interfaces.AuthService;
import com.stockapp.services.interfaces.UserService;
import com.stockapp.utils.PasswordUtils;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for AuthServiceImpl
 * Tests authentication functionality without using Mockito
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthServiceImplTest {

    private AuthService authService;
    private UserService userService;
    private User testUser;

    @BeforeEach
    public void setUp() {
        authService = new AuthServiceImpl();
        userService = new UserServiceImpl();

        // Create a test user for authentication
        String password = "testPassword123";
        String hashedPassword = PasswordUtils.hashPassword(password);

        testUser = new User(
                "testuser_auth",
                hashedPassword,
                "Test User",
                UserRole.CASHIER,
                null);

        // Delete if exists from previous test
        try {
            User existing = userService.findByUsername("testuser_auth");
            if (existing != null) {
                userService.delete(existing.getId());
            }
        } catch (Exception e) {
            // Ignore
        }

        testUser = userService.create(testUser);
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
    public void testValidateLogin_ShouldReturnUserForValidCredentials() {
        User authenticatedUser = authService.validateLogin("testuser_auth", "testPassword123");

        assertNotNull(authenticatedUser, "Should return user for valid credentials");
        assertEquals("testuser_auth", authenticatedUser.getUserName());
        assertEquals("Test User", authenticatedUser.getFullName());
        assertEquals(UserRole.CASHIER, authenticatedUser.getRole());
    }

    @Test
    @Order(2)
    public void testValidateLogin_ShouldReturnNullForInvalidPassword() {
        User authenticatedUser = authService.validateLogin("testuser_auth", "wrongPassword");

        assertNull(authenticatedUser, "Should return null for invalid password");
    }

    @Test
    @Order(3)
    public void testValidateLogin_ShouldReturnNullForNonExistentUser() {
        User authenticatedUser = authService.validateLogin("nonexistent_user", "anyPassword");

        assertNull(authenticatedUser, "Should return null for non-existent user");
    }

    @Test
    @Order(4)
    public void testValidateLogin_ShouldReturnNullForEmptyPassword() {
        User authenticatedUser = authService.validateLogin("testuser_auth", "");

        assertNull(authenticatedUser, "Should return null for empty password");
    }

    @Test
    @Order(5)
    public void testValidateLogin_ShouldBeCaseSensitiveForPassword() {
        User authenticatedUser = authService.validateLogin("testuser_auth", "TESTPASSWORD123");

        assertNull(authenticatedUser, "Password should be case-sensitive");
    }

    @Test
    @Order(6)
    public void testValidateLogin_ShouldReturnCompleteUserObject() {
        User authenticatedUser = authService.validateLogin("testuser_auth", "testPassword123");

        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.getId() > 0, "User should have valid ID");
        assertNotNull(authenticatedUser.getCreatedAt(), "User should have creation timestamp");
        assertNotNull(authenticatedUser.getPasswordHash(), "User should have password hash");
    }
}
