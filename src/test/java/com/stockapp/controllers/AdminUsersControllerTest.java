package com.stockapp.controllers;

import com.stockapp.models.entities.User;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

public class AdminUsersControllerTest extends ApplicationTest {

    private AdminUsersController controller;

    @Override
    public void start(Stage stage) throws Exception {
        var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/AdminUsersDashboard.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    @BeforeEach
    public void setUp() throws Exception {

        interact(() -> controller.setLoggedUser("TestUser"));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testUserLabelIsSet() {
        Label label = lookup("#userNameLabel").queryAs(Label.class);
        assertNotNull(label);
        assertEquals("Hi, TestUser", label.getText());
    }

    @Test
    public void testAddButtonOpensForm() {
        Button addButton = lookup("#addButton").queryAs(Button.class);
        assertNotNull(addButton);
        clickOn(addButton);
        WaitForAsyncUtils.waitForFxEvents();

        boolean formOpened = listTargetWindows().stream()
                .filter(w -> w instanceof Stage)
                .map(w -> (Stage) w)
                .anyMatch(stage -> "User Form".equals(stage.getTitle()));

        assertTrue(formOpened, "User Form should open when Add button is clicked");
    }

    @Test
    public void testModifyButtonRequiresSelection() {
        Button modifyButton = lookup("#modifyButton").queryAs(Button.class);
        assertNotNull(modifyButton);

        // No selection: should trigger alert (does nothing now)
        clickOn(modifyButton);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testDeleteButtonRequiresSelection() {
        Button deleteButton = lookup("#deleteButton").queryAs(Button.class);
        assertNotNull(deleteButton);

        clickOn(deleteButton);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testSignOutButtonWorks() {
        Button signOut = lookup("#sighOutButton").queryAs(Button.class);
        assertNotNull(signOut);

        clickOn(signOut);
        WaitForAsyncUtils.waitForFxEvents();

        boolean loginWindowOpened = listTargetWindows().stream()
                .filter(w -> w instanceof Stage)
                .map(w -> (Stage) w)
                .anyMatch(stage -> "Login".equals(stage.getTitle()));

        assertTrue(loginWindowOpened, "Login window should open after sign out");
    }

    @Test
    public void testUsersTableHasColumns() {
        TableView<User> table = lookup("#usersTable").queryAs(TableView.class);
        assertNotNull(table);
        assertNotNull(table.getColumns().stream().filter(c -> "Id".equals(c.getText())).findFirst().orElse(null));
        assertNotNull(table.getColumns().stream().filter(c -> "Username".equals(c.getText())).findFirst().orElse(null));
        assertNotNull(table.getColumns().stream().filter(c -> "Full Name".equals(c.getText())).findFirst().orElse(null));
        assertNotNull(table.getColumns().stream().filter(c -> "Role".equals(c.getText())).findFirst().orElse(null));
        assertNotNull(table.getColumns().stream().filter(c -> "Created At".equals(c.getText())).findFirst().orElse(null));
    }

}
