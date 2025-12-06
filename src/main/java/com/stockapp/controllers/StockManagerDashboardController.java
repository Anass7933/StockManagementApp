package com.stockapp.controllers;

import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.User;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.ImageView;

public class StockManagerDashboardController {
	@FXML
	private Label userNameLabel;
	@FXML
	private Button addButton;
	@FXML
	private Button modifyButton;
	@FXML
	private Button deleteButton;
	@FXML
	private Button restockButton;
	@FXML
	private TableView<Product> productsTable;
	@FXML
	private TableColumn<Product, Long> idColumn;
	@FXML
	private TableColumn<Product, String> nameColumn;
	@FXML
	private TableColumn<Product, BigDecimal> priceColumn;
	@FXML
	private TableColumn<Product, Integer> quantityColumn;
	@FXML
	private TableColumn<Product, String> categoryColumn;
	@FXML
	private TableColumn<Product, String> stockCheckColumn;
	@FXML
	private Label totalProductsLabel;
	@FXML
	private Label lowStockLabel;
	@FXML
	private Label inStockLabel;
	@FXML
	private Label outOfStockLabel;
	@FXML
	private Button sighOutButton;
	@FXML
	private Button salesButton;
	@FXML
	private ImageView refreshButton;
	@FXML
	private User loggedUser;
	// Auto-refresh timeline for table data only
	private final Timeline refreshTimeline = new Timeline(new KeyFrame(Duration.ZERO, e -> refreshProducts()),
			new KeyFrame(Duration.seconds(2), e -> refreshProducts()));


	public void setLoggedUser(String username) {
		userNameLabel.setText("Hi, " + username);
	}

	@FXML
	private void initialize() {
		try {
			new ProductServiceImpl().refreshStats();
		} catch (Exception e) {
			System.err.println("Warning: Could not refresh product stats: " + e.getMessage());
		}
		idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
		quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
		stockCheckColumn.setCellValueFactory(cellData -> {
			Product product = cellData.getValue();
			ProductService productService = new ProductServiceImpl();
			boolean needsRestock = productService.isNeedRestock(product.getId());
			String value = needsRestock ? "Need Restock" : "Stable";
			return new ReadOnlyStringWrapper(value);
		});
		productsTable.setRowFactory(tv -> new TableRow<>() {
			@Override
			protected void updateItem(Product product, boolean empty) {
				super.updateItem(product, empty);
				if (empty || product == null) {
					setStyle("");
				} else if (product.getQuantity() <= product.getMinStock()) {
					setStyle("-fx-background-color: rgba(255,0,0,0.15);");
				} else {
					setStyle("");
				}
			}
		});
		productsTable.setFixedCellSize(40);
		addButton.setOnAction(e -> openProductForm(0));
		modifyButton.setOnAction(e -> {
			Product selected = productsTable.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert("Select a product first.");
				return;
			}
			openProductForm(selected.getId());
		});
		deleteButton.setOnAction(e -> {
			Product selected = productsTable.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert("Select a product to delete.");
				return;
			}
			try {
				ProductService productService = new ProductServiceImpl();
				productService.delete(selected.getId());
				refreshProducts();
			} catch (Exception ex) {
				String errorMsg = ex.getMessage();
				if (ex.getCause() != null) {
					errorMsg = ex.getCause().getMessage();
				}
				if (errorMsg != null && (errorMsg.contains("foreign key") || errorMsg.contains("sale_items")
						|| errorMsg.contains("violates"))) {
					showAlert("Cannot delete this product because it has been sold. Consider modifying it instead.");
				} else {
					showAlert("Error deleting product: " + errorMsg);
				}
			}
		});
		restockButton.setOnAction(e -> {
			Product selected = productsTable.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert("Select a product first.");
				return;
			}
			openRestockForm(selected.getId());
		});

		salesButton.setOnAction(e -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StockManagerSalesDashboard.fxml"));
				Parent root = loader.load();
				StockManagerSalesDashboardController controller = loader.getController();
				controller.setLoggedUser(userNameLabel.getText().replace("Hi, ", ""));
				Stage stage = (Stage) salesButton.getScene().getWindow();
				stage.setScene(new Scene(root));
				stage.show();
			} catch (Exception ex) {
				ex.printStackTrace();
				showAlert("Error loading Sales dashboard: " + ex.getMessage());
			}
		});
		sighOutButton.setOnAction(e -> signOut());
		refreshButton.setOnMouseClicked(e -> refreshAnalytics());
		refreshTimeline.setCycleCount(Animation.INDEFINITE);
		refreshTimeline.play();
		stat();
	}

	private void refreshAnalytics() {
		ProductServiceImpl productService = new ProductServiceImpl();
		productService.refreshStats();
		stat();
	}

	private void openProductForm(long productId) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductForm.fxml"));
			Parent root = loader.load();
			ProductFormController controller = loader.getController();
			Stage stage = new Stage();
			stage.setTitle(productId == 0 ? "Add Product" : "Edit Product");
			stage.setResizable(false);
			stage.setScene(new Scene(root));
			controller.setStage(stage);
			if (productId != 0) {
				controller.loadProductData(productId);
			}
			stage.setOnHidden(e -> refreshProducts());
			stage.show();
		} catch (IOException e) {
			throw new RuntimeException("Unable to open product form", e);
		}
	}

	private void openRestockForm(long productId) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RestockForm.fxml"));
		Parent root;
		try {
			root = loader.load();
		} catch (IOException e) {
			throw new RuntimeException("Unable to open restock form", e);
		}
		RestockFormController controller = loader.getController();
		Stage stage = new Stage();
		controller.setStage(stage);
		controller.setProductId(productId);
		stage.setTitle("Restock Product");
		stage.setResizable(false);
		stage.setScene(new Scene(root));
		stage.setOnHidden(e -> refreshProducts());
		stage.show();
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

	private void refreshProducts() {
		Product selected = productsTable.getSelectionModel().getSelectedItem();
		ProductService productService = new ProductServiceImpl();
		List<Product> products = productService.readAll();
		ObservableList<Product> data = FXCollections.observableArrayList(products);
		productsTable.setItems(data);

		if (selected != null) {
			data.stream()
					.filter(p -> p.getId() == selected.getId())
					.findFirst()
					.ifPresent(p -> productsTable.getSelectionModel().select(p));
		}
	}

	private void stat() {
		ProductServiceImpl productServiceImpl = new ProductServiceImpl();
		totalProductsLabel.setText(String.valueOf(productServiceImpl.totalProducts()));
		lowStockLabel.setText(String.valueOf(productServiceImpl.lowStock()));
		inStockLabel.setText(String.valueOf(productServiceImpl.inStock()));
		outOfStockLabel.setText(String.valueOf(productServiceImpl.outOfStock()));
	}
}
