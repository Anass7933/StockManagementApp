package com.stockapp.controllers.cashier;

import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.models.enums.Category;
import com.stockapp.utils.CartManager;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CartViewControllerTest extends ApplicationTest {

    private CartViewController controller;
    private CartManager cartManager;

    @Override
    public void start(Stage stage) throws Exception {
        var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/cashier/CartView.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    @BeforeEach
    public void setupCart() {
        cartManager = new CartManager();

        Product product1 = new Product(
                1L,
                "Product 1",
                "Desc 1",
                BigDecimal.valueOf(30.0),
                100,
                5,
                null,
                Category.BOOKS
        );
        Product product2 = new Product(
                2L,
                "Product 2",
                "Desc 2",
                BigDecimal.valueOf(15.0),
                50,
                3,
                null,
                Category.BOOKS
        );

        cartManager.addItem(product1, 2);
        cartManager.addItem(product2, 3);

        interact(() -> controller.setCartManager(cartManager));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testCartTotalUpdates() {
        double total = cartManager.getTotalPrice();
        Label totalLabel = lookup("#totalPriceLabel").queryAs(Label.class);
        assertNotNull(totalLabel, "Total price label should exist");
        assertEquals(String.format("$%.2f", total), totalLabel.getText());
    }

    @Test
    public void testRemoveItem() {
        ListView<SaleItem> listView = lookup("#cartListView").queryAs(ListView.class);
        assertNotNull(listView, "Cart list view should exist");

        if (listView.getItems().isEmpty()) {
            fail("Cart should have items for this test");
        }

        SaleItem itemToRemove = listView.getItems().get(0);
        int initialSize = listView.getItems().size();

        // Run the blocking dialog call asynchronously
        WaitForAsyncUtils.asyncFx(() -> controller.handleRemoveItem(itemToRemove));
        WaitForAsyncUtils.waitForFxEvents();

        // Interact with the dialog (Click OK)
        clickOn("OK");
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(initialSize - 1, listView.getItems().size(), "List should have one less item");
    }

    @Test
    public void testUpdateItemQuantity() {
        SaleItem item = cartManager.getCartItems().get(0);

        // Update quantity via CartManager directly
        interact(() -> {
            cartManager.updateItemQuantity(item, 5);
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(5, item.getQuantity());

        Label totalLabel = lookup("#totalPriceLabel").queryAs(Label.class);
        assertNotNull(totalLabel);
        assertEquals(String.format("$%.2f", cartManager.getTotalPrice()), totalLabel.getText());
    }

    @Test
    public void testEmptyCartMessage() {
        interact(() -> {
            cartManager.clearCart();
            // Re-set manager to trigger checkEmptyCart since it's not triggered by list
            // listener
            controller.setCartManager(cartManager);
        });
        WaitForAsyncUtils.waitForFxEvents();

        ListView<SaleItem> listView = lookup("#cartListView").queryAs(ListView.class);
        assertNotNull(listView);
        assertTrue(listView.getItems().isEmpty(), "Cart should be empty");

        // Check placeholder
        Label placeholder = (Label) listView.getPlaceholder();
        assertNotNull(placeholder, "Placeholder should be set when cart is empty");
        assertEquals("Your cart is empty", placeholder.getText());
    }

    @Test
    public void testCloseCartWindow() {
        Button closeButton = lookup("#closeButton").queryAs(Button.class);
        assertNotNull(closeButton, "Close button should exist");

        Stage stage = (Stage) closeButton.getScene().getWindow();
        assertTrue(stage.isShowing(), "Stage should be showing before close");

        clickOn(closeButton);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(stage.isShowing(), "Stage should be closed after clicking close button");
    }

    @Test
    public void testCartListViewExists() {
        ListView<SaleItem> listView = lookup("#cartListView").queryAs(ListView.class);
        assertNotNull(listView, "Cart list view must exist");
    }

    @Test
    public void testTotalPriceLabelExists() {
        Label totalLabel = lookup("#totalPriceLabel").queryAs(Label.class);
        assertNotNull(totalLabel, "Total price label must exist");
    }

    @Test
    public void testCartInitiallyHasItems() {
        ListView<SaleItem> listView = lookup("#cartListView").queryAs(ListView.class);
        assertNotNull(listView);
        assertFalse(listView.getItems().isEmpty(), "Cart should have items after setup");
    }
}
