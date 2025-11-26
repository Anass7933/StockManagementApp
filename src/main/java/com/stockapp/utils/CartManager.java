package com.stockapp.utils;

import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;

public class CartManager {

	private static CartManager instance;
	private ObservableList<SaleItem> cartItems;

	private CartManager() {
		cartItems = FXCollections.observableArrayList();
	}

	public static CartManager getInstance() {
		if (instance == null) {
			instance = new CartManager();
		}
		return instance;
	}

	public void addItem(Product product, int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than 0");
		}

		if (quantity > product.getQuantity()) {
			throw new IllegalArgumentException("Quantity exceeds available stock");
		}

		// Check if product already in cart
		Optional<SaleItem> existingItem = findItemByProduct(product);

		if (existingItem.isPresent()) {
			// Update existing item quantity
			SaleItem item = existingItem.get();
			int newQuantity = item.getQuantity() + quantity;

			if (newQuantity > product.getQuantity()) {
				throw new IllegalArgumentException("Total quantity exceeds available stock");
			}

			item.setQuantity(newQuantity);
		} else {
			// Create new sale item
			SaleItem newItem = new SaleItem();
			newItem.setProduct(product);
			newItem.setQuantity(quantity);
			newItem.setUnitPrice(product.getPrice().doubleValue());

			cartItems.add(newItem);
		}
	}

	public void removeItem(SaleItem item) {
		cartItems.remove(item);
	}

	public void updateItemQuantity(SaleItem item, int newQuantity) {
		if (newQuantity <= 0) {
			removeItem(item);
			return;
		}

		ProductService productService = new ProductServiceImpl();
		Product product = productService.read(item.getProductId())
				.orElseThrow(() -> new IllegalArgumentException("Product not found"));
		if (newQuantity > product.getQuantity()) {
			throw new IllegalArgumentException("Quantity exceeds available stock");
		}

		item.setQuantity(newQuantity);
		cartItems.set(cartItems.indexOf(item), item);
	}

	public void incrementQuantity(SaleItem item) {
		updateItemQuantity(item, item.getQuantity() + 1);
	}

	public void decrementQuantity(SaleItem item) {
		updateItemQuantity(item, item.getQuantity() - 1);
	}

	public ObservableList<SaleItem> getCartItems() {
		return cartItems;
	}

	public ObservableList<SaleItem> getSaleItems() {
		return FXCollections.unmodifiableObservableList(cartItems);
	}

	public double getTotalPrice() {
		return cartItems.stream()
				.mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
				.sum();
	}

	public int getTotalItemCount() {
		return cartItems.size();
	}

	public boolean isEmpty() {
		return cartItems.isEmpty();
	}

	public void clearCart() {
		cartItems.clear();
	}

	private Optional<SaleItem> findItemByProduct(Product product) {
		return cartItems.stream()
				.filter(item -> item.getProductId() == product.getId())
				.findFirst();
	}

	public boolean containsProduct(Product product) {
		return findItemByProduct(product).isPresent();
	}

	public int getProductQuantityInCart(Product product) {
		return findItemByProduct(product)
				.map(SaleItem::getQuantity)
				.orElse(0);
	}
}
