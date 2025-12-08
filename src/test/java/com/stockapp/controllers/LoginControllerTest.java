package com.stockapp.controllers;

import com.stockapp.models.entities.User;
import com.stockapp.services.impl.UserServiceImpl;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import com.stockapp.models.enums.UserRole;
import com.stockapp.utils.DatabaseUtils;
import com.stockapp.utils.PasswordUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerTest extends ApplicationTest {

    private final String TEST_MGR_USERNAME = "test_stock_mgr";
    private final String TEST_MGR_PASSWORD = "testPassword123";

    @Override
    public void start(Stage stage) throws Exception {
        var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    @BeforeEach
    public void setUp() {
        try {
            createTestUser();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        interact(() -> {
            TextField usernameField = lookup("#usernameField").queryAs(TextField.class);
            PasswordField passwordField = lookup("#passwordField").queryAs(PasswordField.class);
            Label incorrectLabel = lookup("#incorrectLabel").queryAs(Label.class);

            if (usernameField != null)
                usernameField.clear();
            if (passwordField != null)
                passwordField.clear();
            if (incorrectLabel != null)
                incorrectLabel.setText("");
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    public void tearDown() {
        try {
            deleteTestUser();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTestUser() throws SQLException {
        deleteTestUser();
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        userServiceImpl.create(new User(
                TEST_MGR_USERNAME,
                PasswordUtils.hashPassword(TEST_MGR_PASSWORD),
                "Test Stock Manager",
                UserRole.STOCK_MANAGER
        ));
    }

    private void deleteTestUser() throws SQLException {
        try (Connection conn = DatabaseUtils.getConnection()) {
            String sql = "DELETE FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, TEST_MGR_USERNAME);
                ps.executeUpdate();
            }
        }
    }

    @Test
    public void testSuccessfulAdminLogin() {
        clickOn("#usernameField").write("admin");
        clickOn("#passwordField").write("admin");

        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();

        boolean dashboardOpened = listTargetWindows().stream()
                .filter(w -> w instanceof Stage)
                .map(w -> (Stage) w)
                .anyMatch(stage -> stage.getScene() != null &&
                        stage.getScene().getRoot().lookup("#usersTable") != null);

        assertTrue(dashboardOpened, "Admin dashboard should open with users table");
    }

    @Test
    public void testInvalidLoginShowsError() {
        clickOn("#usernameField").write("wrongUser");
        clickOn("#passwordField").write("wrongPass");
        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();

        Label errorLabel = lookup("#incorrectLabel").queryAs(Label.class);
        assertNotNull(errorLabel, "Error label should exist");
        assertEquals("Invalid username or password.", errorLabel.getText());
    }

    @Test
    public void testCancelButtonClosesStage() {
        Button cancelButton = lookup("#cancelButton").queryAs(Button.class);
        assertNotNull(cancelButton, "Cancel button should exist");

        Stage stage = (Stage) cancelButton.getScene().getWindow();
        assertTrue(stage.isShowing(), "Stage should be showing before cancel");

        clickOn(cancelButton);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(stage.isShowing(), "Stage should be closed after cancel");
    }

    @Test
    public void testStockManagerLogin() {
        clickOn("#usernameField").write(TEST_MGR_USERNAME);
        clickOn("#passwordField").write(TEST_MGR_PASSWORD);
        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();

        boolean dashboardOpened = listTargetWindows().stream()
                .filter(w -> w instanceof Stage)
                .map(w -> (Stage) w)
                .anyMatch(stage -> stage.getScene() != null &&
                        (stage.getTitle() != null && stage.getTitle().contains("Stock Manager")));

        assertTrue(dashboardOpened, "Stock Manager dashboard should open");
    }

    @Test
    public void testEmptyCredentialsShowsError() {
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();

        Label errorLabel = lookup("#incorrectLabel").queryAs(Label.class);
        assertNotNull(errorLabel, "Error label should exist");
        assertEquals("Invalid username or password.", errorLabel.getText());
    }

    @Test
    public void testUsernameFieldExists() {
        TextField usernameField = lookup("#usernameField").queryAs(TextField.class);
        assertNotNull(usernameField, "Username field must exist");
    }

    @Test
    public void testPasswordFieldExists() {
        PasswordField passwordField = lookup("#passwordField").queryAs(PasswordField.class);
        assertNotNull(passwordField, "Password field must exist");
    }

    @Test
    public void testLoginButtonExists() {
        Button loginButton = lookup("#loginButton").queryAs(Button.class);
        assertNotNull(loginButton, "Login button must exist");
    }
}
