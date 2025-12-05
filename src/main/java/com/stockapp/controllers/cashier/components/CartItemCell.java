package com.stockapp.controllers.cashier.components;

import java.math.BigDecimal;

import com.stockapp.controllers.cashier.CartViewController;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

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

		container = new HBox(10);
		container.setAlignment(Pos.CENTER_LEFT);
		container.getStyleClass().add("cart-item-row");

		actionsBox = new HBox(8);
		actionsBox.setAlignment(Pos.CENTER_LEFT);
		actionsBox.setPrefWidth(120);
		actionsBox.setMinWidth(120);
		actionsBox.setMaxWidth(120);
		actionsBox.getStyleClass().add("action-buttons");

		updateButton = new Button("Edit");
		updateButton.getStyleClass().add("update-button");

		removeButton = new Button("✕");
		removeButton.getStyleClass().add("remove-button");

		actionsBox.getChildren().addAll(updateButton, removeButton);

		productNameLabel = new Label();
		productNameLabel.setPrefWidth(300);
		productNameLabel.setMinWidth(300);
		productNameLabel.setMaxWidth(Region.USE_PREF_SIZE);
		productNameLabel.getStyleClass().add("product-name");

		quantityLabel = new Label();
		quantityLabel.setPrefWidth(100);
		quantityLabel.setMinWidth(100);
		quantityLabel.setMaxWidth(100);
		quantityLabel.setAlignment(Pos.CENTER);
		quantityLabel.getStyleClass().add("product-quantity");

		priceLabel = new Label();
		priceLabel.setPrefWidth(100);
		priceLabel.setMinWidth(100);
		priceLabel.setMaxWidth(100);
		priceLabel.setAlignment(Pos.CENTER);
		priceLabel.getStyleClass().add("product-price");

		totalLabel = new Label();
		totalLabel.setPrefWidth(100);
		totalLabel.setMinWidth(100);
		totalLabel.setMaxWidth(100);
		totalLabel.setAlignment(Pos.CENTER);
		totalLabel.getStyleClass().add("product-total");

		container.getChildren().addAll(actionsBox, productNameLabel, quantityLabel, priceLabel, totalLabel);
	}

	@Override
	protected void updateItem(SaleItem item, boolean empty) {
		super.updateItem(item, empty);
		ProductService productService = new ProductServiceImpl();
		if (empty || item == null) {
			setGraphic(null);
			setText(null);
		} else {
			productNameLabel.setText(productService.read(item.getProductId()).get().getName());
			quantityLabel.setText(String.valueOf(item.getQuantity()));
			priceLabel.setText(String.format("$%.2f", item.getUnitPrice()));

			BigDecimal total = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

			totalLabel.setText(String.format("$%.2f", total));
			updateButton.setOnAction(e -> {
				e.consume();
				cartViewController.handleUpdateItem(item);
			});
			removeButton.setOnAction(e -> {
				e.consume();
				cartViewController.handleRemoveItem(item);
			});
			setGraphic(container);
			setText(null);
		}
	}
}
