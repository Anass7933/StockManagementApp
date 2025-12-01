package com.stockapp.controllers.cashier;

import com.stockapp.controllers.cashier.components.ProductListCell;
import com.stockapp.models.entities.*;
import com.stockapp.services.impl.*;
import com.stockapp.services.interfaces.*;
import com.stockapp.utils.CartManager;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CashierController {
	@FXML
	private TextFlow cashierTextFlow;
	@FXML
	private Text usernameText;
	@FXML
	private TextField searchField;
	@FXML
	private ToggleButton searchByIdButton;
	@FXML
	private ToggleButton searchByNameButton;
	@FXML
	private ToggleGroup searchTypeGroup;
	@FXML
	private ListView<Product> ProductListView;
	@FXML
	private Button LogOutButton;
	@FXML
	private Button btnCart;
	@FXML
	private Button btnClearCart;
	@FXML
	private Button btnAddSale;
	private ProductService productService;
	private SaleService saleService;
	private CartManager cartManager;
	private ObservableList<Product> productList;

	@FXML
	public void initialize() {
		productService = new ProductServiceImpl();
		saleService = new SaleServiceImpl();
		cartManager = CartManager.getInstance();
		cartManager.setOnCartChangeListener(() -> {
			ProductListView.refresh();
			updateCartButton();
		});
		productList = FXCollections.observableArrayList();
		ProductListView.setItems(productList);
		ProductListView.setCellFactory(lv -> new ProductListCell(this));
		loadAllProducts();
		searchField.textProperty().addListener((obs, oldVal, newVal) -> performSearch());
		searchTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> performSearch());
		LogOutButton.setOnAction(e -> handleLogout());
		btnCart.setOnAction(e -> openCartView());
		btnClearCart.setOnAction(e -> handleClearCart());
		btnAddSale.setOnAction(e -> handleAddSale());
		updateCartButton();
	}

	private void loadAllProducts() {
		try {
			List<Product> products = productService.readAll();
			productList.setAll(products);
		} catch (Exception e) {
			showError("Failed to load products", e.getMessage());
		}
	}

	private void performSearch() {
		String searchText = searchField.getText().trim();
		if (searchText.isEmpty()) {
			loadAllProducts();
			return;
		}
		try {
			Optional<Product> result;
			if (searchByIdButton.isSelected()) {
				try {
					long id = Long.parseLong(searchText);
					result = productService.read(id);
				} catch (NumberFormatException e) {
					result = Optional.empty();
				}
			} else {
				result = productService.findByName(searchText);
			}
			productList.setAll(result.map(List::of).orElse(List.of()));
		} catch (Exception e) {
			showError("Search failed", e.getMessage());
		}
	}

	public void openQuantityForm(Product product) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cashier/QuantityForm.fxml"));
			Parent root = loader.load();
			QuantityFormController controller = loader.getController();
			controller.setProduct(product);
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Add to Cart");
			stage.setScene(new Scene(root));
			stage.setResizable(false);
			stage.setWidth(400);
			stage.setHeight(250);
			stage.showAndWait();
			Optional<Integer> quantity = controller.getQuantity();
			if (quantity.isPresent()) {
				addToCart(product, quantity.get());
			}
		} catch (IOException e) {
			showError("Error", "Failed to open quantity form");
			e.printStackTrace();
		}
	}

	private void addToCart(Product product, int quantity) {
		try {
			cartManager.addItem(product, quantity);
			updateCartButton();
			showInfo("Success", "Product added to cart");
		} catch (Exception e) {
			showError("Failed to add to cart", e.getMessage());
		}
	}

	private void openCartView() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cashier/CartView.fxml"));
			Parent root = loader.load();
			CartViewController controller = loader.getController();
			controller.setCartManager(cartManager);
			controller.setOnCartChanged(this::updateCartButton);
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Shopping Cart");
			stage.setScene(new Scene(root));
			stage.showAndWait();
		} catch (IOException e) {
			showError("Error", "Failed to open cart view");
			e.printStackTrace();
		}
	}

	private void handleClearCart() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Clear Cart");
		alert.setHeaderText("Are you sure?");
		alert.setContentText("This will remove all items from the cart.");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			cartManager.clearCart();
			updateCartButton();
			showInfo("Success", "Cart cleared");
		}
	}

	private void handleAddSale() {
		if (cartManager.isEmpty()) {
			showWarning("Cart Empty", "Please add items to cart before completing sale");
			return;
		}
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Complete Sale");
		alert.setHeaderText("Process this sale?");
		alert.setContentText(String.format("Total: %.2f$", cartManager.getTotalPrice()));
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			try {
				List<SaleItem> items = cartManager.getCartItems();
				Sale sale = new Sale((long) cartManager.getTotalPrice());
				saleService.createSaleWithItems(sale, items);
				cartManager.clearCart();
				updateCartButton();
				loadAllProducts();
				showInfo("Success", "Sale completed successfully");
			} catch (Exception e) {
				showError("Sale Failed", e.getMessage());
			}
		}
	}

	private void updateCartButton() {
		int itemCount = cartManager.getTotalItemCount();
		btnCart.setText(String.format("Cart (%d)", itemCount));
	}

	private void handleLogout() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Logout");
		alert.setHeaderText("Are you sure you want to logout?");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			try {
				cartManager.clearCart();
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
				Parent root = loader.load();
				Stage stage = (Stage) LogOutButton.getScene().getWindow();
				stage.setScene(new Scene(root));
			} catch (IOException e) {
				showError("Error", "Failed to logout");
				e.printStackTrace();
			}
		}
	}

	private void showError(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void showInfo(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void showWarning(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
