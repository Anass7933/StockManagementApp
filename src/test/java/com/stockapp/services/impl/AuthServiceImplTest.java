package com.stockapp.services.impl;

import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.services.interfaces.AuthService;
import com.stockapp.services.interfaces.UserService;
import com.stockapp.utils.PasswordUtils;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthServiceImplTest {

    private AuthService authService;
    private UserService userService;
    private User testUser;

    @BeforeAll
    void initServices() {
        authService = new AuthServiceImpl();
        userService = new UserServiceImpl();
    }

    @BeforeEach
    void setUp() {
        // Delete user if already exists
        try {
            User existing = userService.findByUsername("testuser_auth");
            if (existing != null) {
                userService.delete(existing.getId());
            }
        } catch (Exception ignored) {}

        // Create fresh test user
        String rawPassword = "testPassword123";
        String hashed = PasswordUtils.hashPassword(rawPassword);

        testUser = new User(
                "testuser_auth",
                hashed,
                "Test User",
                UserRole.CASHIER,
                null
        );

        testUser = userService.create(testUser);
    }

    @AfterEach
    void tearDown() {
        try {
            if (testUser != null && testUser.getId() > 0) {
                userService.delete(testUser.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(1)
    void validCredentials_ReturnUser() {
        User user = authService.validateLogin("testuser_auth", "testPassword123");

        assertNotNull(user);
        assertEquals("testuser_auth", user.getUserName());
        assertEquals(UserRole.CASHIER, user.getRole());
    }

    @Test
    @Order(2)
    void wrongPassword_ReturnNull() {
        assertNull(authService.validateLogin("testuser_auth", "wrongPassword"));
    }

    @Test
    @Order(3)
    void unknownUser_ReturnNull() {
        assertNull(authService.validateLogin("nope", "whatever"));
    }

    @Test
    @Order(4)
    void emptyPassword_ReturnNull() {
        assertNull(authService.validateLogin("testuser_auth", ""));
    }

    @Test
    @Order(5)
    void passwordCaseSensitive() {
        assertNull(authService.validateLogin("testuser_auth", "TESTPASSWORD123"));
    }

    @Test
    @Order(6)
    void returnedUser_HasRequiredFields() {
        User user = authService.validateLogin("testuser_auth", "testPassword123");

        assertNotNull(user);
        assertTrue(user.getId() > 0);
        assertNotNull(user.getPasswordHash());
        assertNotNull(user.getCreatedAt());
        assertNotEquals("testPassword123", user.getPasswordHash());
    }

    @Test
    @Order(7)
    void nullInputs_ReturnNull() {
        assertNull(authService.validateLogin(null, "abc"));
        assertNull(authService.validateLogin("testuser_auth", null));
        assertNull(authService.validateLogin(null, null));
    }
}
