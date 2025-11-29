package com.stockapp.controllers.cashier.components;

import com.stockapp.controllers.cashier.CashierController;
import com.stockapp.models.entities.Product;
import com.stockapp.utils.CartManager;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Custom ListCell for displaying products in the cashier view.
 * Shows product information and an "Add to Cart" button.
 */
public class ProductListCell extends ListCell<Product> {

	private final HBox container;
	private final VBox infoContainer;
	private final Label nameLabel;
	private final HBox detailsRow;
	private final Label idLabel;
	private final Label categoryLabel;
	private final Label priceLabel;
	private final Label stockLabel;
	private final Button addButton;
	private final Region spacer;

	private final CashierController cashierController;

	public ProductListCell(CashierController cashierController) {
		this.cashierController = cashierController;

		// Main container
		container = new HBox(15);
		container.setAlignment(Pos.CENTER_LEFT);
		container.getStyleClass().add("product-cell-container");

		// Info container (left side)
		infoContainer = new VBox(6);
		infoContainer.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(infoContainer, Priority.ALWAYS);

		// Product name
		nameLabel = new Label();
		nameLabel.getStyleClass().add("product-name");

		// Details row (ID, Category, Price, Stock)
		detailsRow = new HBox(20);
		detailsRow.setAlignment(Pos.CENTER_LEFT);
		detailsRow.getStyleClass().add("product-details");

		idLabel = new Label();
		idLabel.getStyleClass().add("product-id");

		categoryLabel = new Label();
		categoryLabel.getStyleClass().add("product-category");

		priceLabel = new Label();
		priceLabel.getStyleClass().add("product-price");

		stockLabel = new Label();
		stockLabel.getStyleClass().add("product-stock");

		detailsRow.getChildren().addAll(idLabel, categoryLabel, priceLabel, stockLabel);

		infoContainer.getChildren().addAll(nameLabel, detailsRow);

		// Spacer
		spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		// Add to Cart button (right side)
		addButton = new Button("Add to Cart");
		addButton.getStyleClass().add("add-to-cart-button");

		container.getChildren().addAll(infoContainer, spacer, addButton);
	}

	@Override
	protected void updateItem(Product product, boolean empty) {
		super.updateItem(product, empty);

		if (empty || product == null) {
			setGraphic(null);
			setText(null);
		} else {
			// Update product information
			nameLabel.setText(product.getName());
			idLabel.setText("ID: " + product.getId());
			categoryLabel.setText(product.getCategory().toString());
			priceLabel.setText(String.format("$%.2f", product.getPrice()));

			// --- NEW LOGIC STARTS HERE ---

			// 1. Get Actual DB Stock
			int actualStock = product.getQuantity();
			int minStock = product.getMinStock();

			// 2. Get Quantity currently inside the Cart
			int inCart = CartManager.getInstance().getProductQuantityInCart(product);

			// 3. Calculate Visual Stock (prevent negatives)
			int displayStock = Math.max(0, actualStock - inCart);

			// Reset stock label styles
			stockLabel.getStyleClass().removeAll("product-stock", "product-stock-low", "product-stock-out");

			// 4. Use 'displayStock' for all visual logic
			if (displayStock == 0) {
				stockLabel.setText("Out of Stock");
				stockLabel.getStyleClass().add("product-stock-out");
				// Important: Disable button if visual stock is 0 (even if DB has stock)
				addButton.setDisable(true);
			} else if (displayStock <= minStock) {
				stockLabel.setText("Stock: " + displayStock);
				stockLabel.getStyleClass().add("product-stock-low");
				addButton.setDisable(false);
			} else {
				stockLabel.setText("Stock: " + displayStock);
				stockLabel.getStyleClass().add("product-stock");
				addButton.setDisable(false);
			}

			// Button click handler
			addButton.setOnAction(e -> {
				e.consume(); // Prevent list selection
				cashierController.openQuantityForm(product);
			});

			setGraphic(container);
			setText(null);
		}
	}
}
