package com.stockapp.controllers.cashier;

import com.stockapp.models.entities.Product;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class QuantityFormController {
	@FXML
	private Label lblProductName;
	@FXML
	private TextField txtQuantity;
	@FXML
	private Label lblMaxStock;
	@FXML
	private Button btnCancel;
	@FXML
	private Button btnAdd;
	private Product product;
	private Integer selectedQuantity;

	@FXML
	public void initialize() {
		txtQuantity.setText("1");
		txtQuantity.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				txtQuantity.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});
		btnCancel.setOnAction(e -> handleCancel());
		btnAdd.setOnAction(e -> handleAdd());
		txtQuantity.setOnAction(e -> handleAdd());
	}

	public void setProduct(Product product) {
		this.product = product;
		lblProductName.setText(product.getName());
		lblMaxStock.setText("Max: " + product.getQuantity());
		txtQuantity.requestFocus();
		txtQuantity.selectAll();
	}

	private void handleAdd() {
		if (validateInput()) {
			selectedQuantity = Integer.parseInt(txtQuantity.getText());
			closeDialog();
		}
	}

	private void handleCancel() {
		selectedQuantity = null;
		closeDialog();
	}

	private boolean validateInput() {
		String input = txtQuantity.getText().trim();
		if (input.isEmpty()) {
			showError("Invalid Input", "Please enter a quantity.");
			return false;
		}
		try {
			int quantity = Integer.parseInt(input);
			if (quantity <= 0) {
				showError("Invalid Quantity", "Quantity must be greater than 0.");
				return false;
			}
			if (quantity > product.getQuantity()) {
				showError("Insufficient Stock",
						String.format("Only %d units available in stock.", product.getQuantity()));
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			showError("Invalid Input", "Please enter a valid number.");
			return false;
		}
	}

	public Optional<Integer> getQuantity() {
		return Optional.ofNullable(selectedQuantity);
	}

	private void closeDialog() {
		Stage stage = (Stage) btnAdd.getScene().getWindow();
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
