
package com.stockapp.controllers;

import com.stockapp.models.entities.Sale;
import com.stockapp.models.entities.User;
import com.stockapp.services.impl.SaleServiceImpl;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

public class StockManagerSalesDashboardController {
    @FXML
    private Label userNameLabel;
    @FXML
    private TableView<Sale> salesTable;
    @FXML
    private TableColumn<Sale, Long> idColumn;
    @FXML
    private TableColumn<Sale, String> dateColumn;
    @FXML
    private TableColumn<Sale, BigDecimal> totalAmountColumn;
    @FXML
    private TableColumn<Sale, String> totalItemsColumn;
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
    @FXML
    private ImageView refreshButton;
    @FXML
    private User loggedUser;

    public void setLoggedUser(String username) {
        userNameLabel.setText("Hi, " + username);
    }

    @FXML
    private void initialize() {
        loadSalesTable();

        stat();

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
        refreshButton.setOnMouseClicked(e -> refreshAnalytics());

        sighOutButton.setOnAction(e -> signOut());
    }

    private void refreshAnalytics() {
        SaleServiceImpl saleService = new SaleServiceImpl();
        saleService.refreshStats();
        stat();
    }

    private void loadSalesTable() {
        SaleServiceImpl saleService = new SaleServiceImpl();
        List<Sale> sales = saleService.readAll();

        ObservableList<Sale> data = FXCollections.observableArrayList(sales);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        dateColumn.setCellValueFactory(cellData -> {
            OffsetDateTime dt = cellData.getValue().getCreatedAt();
            String formatted = dt != null
                    ? dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    : "";
            return new SimpleStringProperty(formatted);
        });

        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        totalItemsColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getTotalItems())));

        salesTable.setItems(data);
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


    private void stat() {
        SaleServiceImpl saleServiceImpl = new SaleServiceImpl();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        totalSalesLabel.setText(String.valueOf(saleServiceImpl.totalSales(startDate, endDate)));
        totalRevenueLabel.setText(String.valueOf(saleServiceImpl.totalRevenue(startDate, endDate)));
        totalItemsSoldLabel.setText(String.valueOf(saleServiceImpl.totalItemsSold(startDate, endDate)));
        averageSaleValueLabel.setText(String.valueOf(saleServiceImpl.averageSaleValue(startDate, endDate)));
    }
}
