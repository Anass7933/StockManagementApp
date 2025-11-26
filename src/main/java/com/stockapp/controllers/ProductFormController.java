package com.stockapp.controllers;

import com.stockapp.models.entities.Product;
import com.stockapp.models.enums.Category;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class ProductFormController {

	@FXML
	private TextField nameField;
	@FXML
	private TextField priceField;
	@FXML
	private TextField quantityField;
	@FXML
	private ComboBox<Category> categoryField; // <-- changed from TextField
	@FXML
	private TextField minStockField;
	@FXML
	private TextArea descriptionField;
	@FXML
	private Button saveButton;
	@FXML
	private Button cancelButton;

	private Stage stage;

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	private void initialize() {
		// Populate ComboBox with enum values
		categoryField.getItems().setAll(Category.values());
		categoryField.getSelectionModel().selectFirst(); // optional default selection

		saveButton.setOnAction(e -> saveProduct());
	}

	private long editingProductId;

	private void saveProduct() {
		ProductService productService = new ProductServiceImpl();
		try {
			String name = nameField.getText().trim();
			BigDecimal price = new BigDecimal(priceField.getText().trim());
			int quantity = Integer.parseInt(quantityField.getText().trim());
			int minStock = Integer.parseInt(minStockField.getText().trim());
			String description = descriptionField.getText().trim();
			Category category = categoryField.getValue();

			if (category == null) {
				showAlert("Please select a category.");
				return;
			}

			Product product = new Product(name, description, price, quantity, minStock, category);

			if (editingProductId == 0) {
				productService.create(product);
			} else {
				product.setId(editingProductId);
				productService.update(product);
			}

			stage.close();
		} catch (NumberFormatException ex) {
			showAlert("Please enter valid numeric values for price, quantity and min stock.");
		}
	}

	public void loadProductData(long productId) {
		editingProductId = productId;
		ProductService productService = new ProductServiceImpl();
		productService.read(productId).ifPresent(product -> {
			nameField.setText(product.getName());
			priceField.setText(product.getPrice().toPlainString());
			quantityField.setText(String.valueOf(product.getQuantity()));
			categoryField.setValue(product.getCategory()); // ComboBox uses setValue()
			minStockField.setText(String.valueOf(product.getMinStock()));
			descriptionField.setText(product.getDescription());
		});
	}

	private void showAlert(String msg) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setHeaderText(null);
		alert.setContentText(msg);
		alert.showAndWait();
	}

	public void cancelButtonOnAction(ActionEvent actionEvent) {
		stage.close();
	}
}
