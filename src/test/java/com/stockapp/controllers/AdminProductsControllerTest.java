package com.stockapp.controllers;

import com.stockapp.models.entities.Product;
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

public class AdminProductsControllerTest extends ApplicationTest {

    private AdminProductsController controller;

    @Override
    public void start(Stage stage) throws Exception {
        var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/AdminProductsDashboard.fxml"));
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

    /**
     * Test that the user label is set correctly.
     */
    @Test
    public void testUserLabelIsSet() {
        Label label = lookup("#userNameLabel").queryAs(Label.class);
        assertNotNull(label, "User label must exist");
        assertEquals("Hi, TestUser", label.getText());
    }

    /**
     * Test that the "Add Product" button opens the product form.
     */
    @Test
    public void testAddButtonOpensForm() {
        Button addButton = lookup("#addButton").queryAs(Button.class);
        assertNotNull(addButton, "Add button must exist");

        clickOn(addButton);
        WaitForAsyncUtils.waitForFxEvents();

        boolean formOpened = listTargetWindows().stream()
                .filter(w -> w instanceof Stage)
                .map(w -> (Stage) w)
                .anyMatch(stage -> "Add Product".equals(stage.getTitle()));

        assertTrue(formOpened, "Add Product form should open");
    }

    /**
     * Test that sign-out opens the login window.
     */
    @Test
    public void testSignOutButtonWorks() {
        Button signOut = lookup("#sighOutButton").queryAs(Button.class);
        assertNotNull(signOut, "SignOut button must exist");

        clickOn(signOut);
        WaitForAsyncUtils.waitForFxEvents();

        boolean loginWindowOpened = listTargetWindows().stream()
                .filter(w -> w instanceof Stage)
                .map(w -> (Stage) w)
                .anyMatch(stage -> "Login".equals(stage.getTitle()));

        assertTrue(loginWindowOpened, "Login window should open after sign out");
    }

    /**
     * Test that the products table has all required columns.
     */
    @Test
    public void testProductsTableHasColumns() {
        TableView<Product> table = lookup("#productsTable").queryAs(TableView.class);
        assertNotNull(table, "Products table must not be null");

        assertNotNull(table.getColumns().stream().filter(c -> "Id".equals(c.getText())).findFirst().orElse(null));
        assertNotNull(table.getColumns().stream().filter(c -> "Name".equals(c.getText())).findFirst().orElse(null));
        assertNotNull(table.getColumns().stream().filter(c -> "Price".equals(c.getText())).findFirst().orElse(null));
        assertNotNull(table.getColumns().stream().filter(c -> "Qty".equals(c.getText())).findFirst().orElse(null));
        assertNotNull(table.getColumns().stream().filter(c -> "Category".equals(c.getText())).findFirst().orElse(null));
    }

}
