package com.stockapp.controllers;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import com.stockapp.models.Product;
import com.stockapp.services.ProductService;
import javafx.stage.Stage;

public class StockManagerDashboardController {

	@FXML private ListView<String> navList;
	@FXML private StackPane contentPane;
	@FXML private Button usersButton;
	@FXML private Button sighOutButton;

	@FXML public void initialize() {
		navList.getItems().addAll("Products","Sales");

		navList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			contentPane.getChildren().clear();

			if("Products".equals(newVal)) {
				contentPane.getChildren().add(createProductsList());
			}
		});
        usersButton.setOnAction(e -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminUsersDashboard.fxml"));
            Scene scene = null;
            try {
                scene = new Scene(loader.load());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            // Change scene
                Stage stage = (Stage) usersButton.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
		});


		navList.getSelectionModel().selectFirst();
	}
	
	private ListView<String> createProductsList() {
		ListView<String> productsList = new ListView<>();

		ProductService productService = new ProductService();

		List<Product> products = productService.loadProducts();
		for (Product p : products) {
			productsList.getItems().add(p.getName());
		}

		return productsList;
	}


}
