package com.stockapp.controllers;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import com.stockapp.models.Product;
import com.stockapp.models.Sale;
import com.stockapp.services.ProductService;
import com.stockapp.services.SaleService;

public class StockManagerDashboardController {

	@FXML private ListView<String> navList;
	@FXML private StackPane contentPane;

	@FXML public void initialize() {
		navList.getItems().addAll("Products","Sales");

		navList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			contentPane.getChildren().clear();

			if("Products".equals(newVal)) {
				contentPane.getChildren().add(createProductsList());
			}
			/*else if ("Sales".equals(newVal)) {
				contentPane.getChildren().add(createSalesList());
			}

			 */
		});
		navList.getSelectionModel().selectFirst();
	}
	
	private ListView<String> createProductsList() {
		ListView<String> productsList = new ListView<>();

		ProductService productService = new ProductService();

		List<Product> products = productService.getAllProducts();
		for (Product p : products) {
			productsList.getItems().add(p.getName());
		}

		return productsList;
	}

/*	private ListView<String> createSalesList() {
		ListView<String> salesList = new ListView<>();

		SaleService saleService = new SaleService();

		List<Sale> sales = saleService.getAllSales();
		for (Sale s : sales) {
			salesList.getItems().add(""+s.getSaleId());
		}
		return salesList;
	}

 */
}
