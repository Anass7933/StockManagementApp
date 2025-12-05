
package com.stockapp.controllers;

import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.User;
import com.stockapp.services.impl.SaleServiceImpl;
import java.io.IOException;
import java.math.BigDecimal;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class StockManagerSalesDashboardController {
    @FXML
    private Label userNameLabel;
    @FXML
    private TableView<Product> salesTable;
    @FXML
    private TableColumn<Product, Long> idColumn;
    @FXML
    private TableColumn<Product, String> dateColumn;
    @FXML
    private TableColumn<Product, BigDecimal> totalItemsColumn;
    @FXML
    private TableColumn<Product, Integer> totalAmountColumn;
    @FXML
    private Label totalSalesLabel;
    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalItemsSoldLabel;
    @FXML
    private Label averageSaleValueLabel;
    @FXML
    private Button sighOutButton;
    @FXML
    private Button productsButton;
    private User loggedUser;

    public void setLoggedUser(String username) {
        userNameLabel.setText("Hi, " + username);
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        totalItemsColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        salesTable.setFixedCellSize(40);

        productsButton.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StockManagerDashboard.fxml"));
                Parent root = loader.load();
                StockManagerDashboardController controller = loader.getController();
                controller.setLoggedUser(userNameLabel.getText().replace("Hi, ", ""));
                Stage stage = (Stage) productsButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        sighOutButton.setOnAction(e -> signOut());
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void signOut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Stage currentStage = (Stage) sighOutButton.getScene().getWindow();
            currentStage.setScene(new Scene(loader.load()));
            currentStage.setTitle("Login");
            currentStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load login screen", e);
        }
    }

    private void autoResizeTable() {
        double headerHeight = 30;
        var header = salesTable.lookup(".column-header-background");
        if (header != null) {
            headerHeight = header.prefHeight(-1);
        }
        int rows = salesTable.getItems().size();
        double totalHeight = headerHeight + rows * salesTable.getFixedCellSize();
        double maxHeight = 500;
        salesTable.setPrefHeight(Math.min(totalHeight, maxHeight));
    }

    private void stat() {
        SaleServiceImpl saleServiceImpl = new SaleServiceImpl();
        totalSalesLabel.setText(String.valueOf(saleServiceImpl.totalSales()));
        totalRevenueLabel.setText(String.valueOf(saleServiceImpl.totalRevenue()));
        totalItemsSoldLabel.setText(String.valueOf(saleServiceImpl.totalItemsSold()));
        averageSaleValueLabel.setText(String.valueOf(saleServiceImpl.averageSaleValue()));

    }
}
