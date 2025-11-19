package com.stockapp.controllers;

import com.stockapp.services.ProductService;
import com.stockapp.utils.DatabaseUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

public class RestockFormController {

    @FXML private TextField restockAmountField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;


    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        saveButton.setOnAction(e -> saveProduct());
    }

    private long productId;

    public void setProductId(long productId) {
        this.productId = productId;
    }

    private void saveProduct() {
        try (Connection conn = DatabaseUtils.getConnection()) {
            int amount = Integer.parseInt(restockAmountField.getText().trim());
            if (amount <= 0) { showAlert("Amount must be positive"); return; }
            ProductService.increaseStock(productId, amount);
            stage.close();
        } catch (Exception ex) {
            showAlert("Failed to restock: " + ex.getMessage());
        }
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
