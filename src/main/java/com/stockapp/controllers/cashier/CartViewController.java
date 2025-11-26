package com.stockapp.controllers.cashier;

import com.stockapp.controllers.cashier.components.CartItemCell;
import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;
import com.stockapp.utils.CartManager;
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

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the shopping cart view dialog.
 * Manages cart items display and operations (update, remove).
 */
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
		// Set custom cell factory for cart items
		cartListView.setCellFactory(lv -> new CartItemCell(this));

		// Close button action
		closeButton.setOnAction(e -> handleClose());

		// Listen for cart changes to update total
		cartListView.getItems().addListener((javafx.collections.ListChangeListener<SaleItem>) c -> {
			updateTotal();
		});
	}

	/**
	 * Set the cart manager and bind the list view to cart items
	 * 
	 * @param cartManager The cart manager instance
	 */
	public void setCartManager(CartManager cartManager) {
		this.cartManager = cartManager;

		// Bind ListView to cart items
		cartListView.setItems(cartManager.getCartItems());

		// Update total price
		updateTotal();

		// Show empty message if cart is empty
		checkEmptyCart();
	}

	/**
	 * Set callback to be called when cart changes (for updating parent controller)
	 * 
	 * @param callback Runnable to execute on cart change
	 */
	public void setOnCartChanged(Runnable callback) {
		this.onCartChangedCallback = callback;
	}

	/**
	 * Handle update item action (opens quantity form to edit quantity)
	 * 
	 * @param item The item to update
	 */
	public void handleUpdateItem(SaleItem item) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cashier/QuantityForm.fxml"));
			Parent root = loader.load();

			QuantityFormController controller = loader.getController();

			// Get the product from the item
			ProductService productService = new ProductServiceImpl();
			Product product = productService.read(item.getProductId()).get();
			controller.setProduct(product);

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Update Quantity");
			stage.setScene(new Scene(root));
			stage.setResizable(false);

			// Handle result when dialog closes
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

	/**
	 * Handle remove item action
	 * 
	 * @param item The item to remove
	 */
	public void handleRemoveItem(SaleItem item) {
		ProductService productService = new ProductServiceImpl();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Remove Item");
		alert.setHeaderText("Remove this item from cart?");
		alert.setContentText(productService.read(item.getProductId()).get().getName());

		Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
			cartManager.removeItem(item);
			updateTotal();
			notifyCartChanged();
			checkEmptyCart();
		}
	}

	/**
	 * Update the total price display
	 */
	private void updateTotal() {
		double total = cartManager.getTotalPrice();
		totalPriceLabel.setText(String.format("$%.2f", total));
	}

	/**
	 * Notify parent controller that cart has changed
	 */
	private void notifyCartChanged() {
		if (onCartChangedCallback != null) {
			onCartChangedCallback.run();
		}
	}

	/**
	 * Check if cart is empty and show/hide empty message
	 */
	private void checkEmptyCart() {
		if (cartManager.isEmpty()) {
			// Show empty cart message
			Label emptyLabel = new Label("Your cart is empty");
			emptyLabel.getStyleClass().add("empty-cart-message");
			cartListView.setPlaceholder(emptyLabel);
		} else {
			cartListView.setPlaceholder(null);
		}
	}

	/**
	 * Handle close button click
	 */
	private void handleClose() {
		Stage stage = (Stage) closeButton.getScene().getWindow();
		stage.close();
	}

	/**
	 * Show error alert
	 */
	private void showError(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
