package com.stockapp.controllers.cashier.components;

import com.stockapp.controllers.cashier.CartViewController;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;
import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.SaleItem;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Custom ListCell for displaying cart items in the cart view.
 * Shows item details with Update and Remove action buttons.
 */
public class CartItemCell extends ListCell<SaleItem> {

	private final HBox container;
	private final HBox actionsBox;
	private final Button updateButton;
	private final Button removeButton;
	private final Label productNameLabel;
	private final Label quantityLabel;
	private final Label priceLabel;
	private final Label totalLabel;

	private final CartViewController cartViewController;

	public CartItemCell(CartViewController cartViewController) {
		this.cartViewController = cartViewController;

		// Main container - matches the header column layout
		container = new HBox(10);
		container.setAlignment(Pos.CENTER_LEFT);
		container.getStyleClass().add("cart-item-row");

		// Actions column (120px width)
		actionsBox = new HBox(8);
		actionsBox.setAlignment(Pos.CENTER_LEFT);
		actionsBox.setPrefWidth(120);
		actionsBox.setMinWidth(120);
		actionsBox.setMaxWidth(120);
		actionsBox.getStyleClass().add("action-buttons");

		// Update button (opens quantity form)
		updateButton = new Button("Edit");
		updateButton.getStyleClass().add("update-button");

		// Remove button
		removeButton = new Button("✕");
		removeButton.getStyleClass().add("remove-button");

		actionsBox.getChildren().addAll(updateButton, removeButton);

		// Product Name column (300px width)
		productNameLabel = new Label();
		productNameLabel.setPrefWidth(300);
		productNameLabel.setMinWidth(300);
		productNameLabel.setMaxWidth(Region.USE_PREF_SIZE);
		productNameLabel.getStyleClass().add("product-name");

		// Quantity column (100px width)
		quantityLabel = new Label();
		quantityLabel.setPrefWidth(100);
		quantityLabel.setMinWidth(100);
		quantityLabel.setMaxWidth(100);
		quantityLabel.setAlignment(Pos.CENTER);
		quantityLabel.getStyleClass().add("product-quantity");

		// Price column (100px width)
		priceLabel = new Label();
		priceLabel.setPrefWidth(100);
		priceLabel.setMinWidth(100);
		priceLabel.setMaxWidth(100);
		priceLabel.setAlignment(Pos.CENTER);
		priceLabel.getStyleClass().add("product-price");

		// Total column (100px width)
		totalLabel = new Label();
		totalLabel.setPrefWidth(100);
		totalLabel.setMinWidth(100);
		totalLabel.setMaxWidth(100);
		totalLabel.setAlignment(Pos.CENTER);
		totalLabel.getStyleClass().add("product-total");

		// Add all columns to container
		container.getChildren().addAll(
				actionsBox,
				productNameLabel,
				quantityLabel,
				priceLabel,
				totalLabel);
	}

	@Override
	protected void updateItem(SaleItem item, boolean empty) {
		super.updateItem(item, empty);
		ProductService productService = new ProductServiceImpl();

		if (empty || item == null) {
			setGraphic(null);
			setText(null);
		} else {
			// Update item information
			productNameLabel.setText(productService.read(item.getProductId()).get().getName());
			quantityLabel.setText(String.valueOf(item.getQuantity()));
			priceLabel.setText(String.format("$%.2f", item.getUnitPrice()));

			// Calculate and display total (quantity * unit price)
			double total = item.getQuantity() * item.getUnitPrice();
			totalLabel.setText(String.format("$%.2f", total));

			// Update button - opens quantity form to edit quantity
			updateButton.setOnAction(e -> {
				e.consume();
				cartViewController.handleUpdateItem(item);
			});

			// Remove button - removes item from cart
			removeButton.setOnAction(e -> {
				e.consume();
				cartViewController.handleRemoveItem(item);
			});

			setGraphic(container);
			setText(null);
		}
	}
}
