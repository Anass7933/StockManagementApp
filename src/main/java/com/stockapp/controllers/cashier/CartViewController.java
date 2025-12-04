package com.stockapp.controllers.cashier;

import com.stockapp.controllers.cashier.components.CartItemCell;
import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;
import com.stockapp.utils.CartManager;
import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CartViewController {
    @FXML
    private VBox cartItemsContainer;
    @FXML
    private ListView<SaleItem> cartListView;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Button closeButton;

    private CartManager cartManager;
    private Runnable onCartChangedCallback;

    @FXML
    public void initialize() {
        cartListView.setCellFactory(lv -> new CartItemCell(this));
        closeButton.setOnAction(e -> handleClose());
        cartListView.getItems().addListener((javafx.collections.ListChangeListener<SaleItem>) c -> updateTotal());
    }

    public void setCartManager(CartManager cartManager) {
        this.cartManager = cartManager;
        cartListView.setItems(cartManager.getCartItems());

        this.cartManager.setOnCartChangeListener(() -> {
            updateTotal();
            checkEmptyCart();
            cartListView.refresh();
        });

        updateTotal();
        checkEmptyCart();
    }

    public void setOnCartChanged(Runnable callback) {
        this.onCartChangedCallback = callback;
    }

    public void handleUpdateItem(SaleItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cashier/QuantityForm.fxml"));
            Parent root = loader.load();
            QuantityFormController controller = loader.getController();
            ProductService productService = new ProductServiceImpl();
            Product product = productService.read(item.getProductId()).get();
            controller.setProduct(product);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Update Quantity");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            Optional<Integer> newQuantity = controller.getQuantity();
            if (newQuantity.isPresent()) {
                try {
                    cartManager.updateItemQuantity(item, newQuantity.get());
                    updateTotal();
                    notifyCartChanged();
                    checkEmptyCart();
                } catch (IllegalArgumentException e) {
                    showError("Update Failed", e.getMessage());
                }
            }
        } catch (IOException e) {
            showError("Error", "Failed to open quantity form");
            e.printStackTrace();
        }
    }

    public void handleRemoveItem(SaleItem item) {
        ProductService productService = new ProductServiceImpl();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Item");
        alert.setHeaderText("Remove this item from cart?");
        alert.setContentText(productService.read(item.getProductId()).get().getName());

        Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            // Remove by productId to avoid object reference issues
            cartManager.getCartItems().removeIf(i -> i.getProductId() == item.getProductId());

            updateTotal();
            notifyCartChanged();
            checkEmptyCart();
        }
    }

    private void updateTotal() {
        double total = cartManager.getTotalPrice();
        totalPriceLabel.setText(String.format("$%.2f", total));
    }

    private void notifyCartChanged() {
        if (onCartChangedCallback != null) {
            onCartChangedCallback.run();
        }
    }

    private void checkEmptyCart() {
        if (cartManager.isEmpty()) {
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.getStyleClass().add("empty-cart-message");
            cartListView.setPlaceholder(emptyLabel);
        } else {
            cartListView.setPlaceholder(null);
        }
    }

    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
