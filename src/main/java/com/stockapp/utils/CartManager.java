package com.stockapp.utils;

import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.management.Query;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartManager {
	private static CartManager instance;
	private ObservableList<SaleItem> cartItems;
	private List<Runnable> cartChangeListeners;

	public CartManager() {
		cartItems = FXCollections.observableArrayList();
		cartChangeListeners = new ArrayList<>();
	}

	public static CartManager getInstance() {
		if (instance == null) {
			instance = new CartManager();
		}
		return instance;
	}

	public void addCartChangeListener(Runnable listener) {
		if (listener != null && !cartChangeListeners.contains(listener)) {
			cartChangeListeners.add(listener);
		}
	}

	public void removeCartChangeListener(Runnable listener) {
		cartChangeListeners.remove(listener);
	}

	private void notifyCartChange() {
		for (Runnable listener : cartChangeListeners) {
			listener.run();
		}
	}

	public void addItem(Product product, int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than 0");
		}
		if (quantity > product.getQuantity()) {
			throw new IllegalArgumentException("Quantity exceeds available stock");
		}
		Optional<SaleItem> existingItem = findItemByProduct(product);
		if (existingItem.isPresent()) {
			SaleItem item = existingItem.get();
			int newQuantity = item.getQuantity() + quantity;
			if (newQuantity > product.getQuantity()) {
				throw new IllegalArgumentException("Total quantity exceeds available stock");
			}
			item.setQuantity(newQuantity);
		} else {
			SaleItem newItem = new SaleItem();
			newItem.setProduct(product);
			newItem.setUnitPrice(product.getPrice());
			newItem.setQuantity(quantity);
			cartItems.add(newItem);
		}
		notifyCartChange();
	}

	public void removeItem(SaleItem item) {
		cartItems.remove(item);
		notifyCartChange();
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
		notifyCartChange();
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

	public BigDecimal getTotalPrice() {
		BigDecimal total = BigDecimal.ZERO;

		for (var item : cartItems) {
			BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

			total = total.add(itemTotal);
		}

		return total;
	}

	public int getTotalItemCount() {
		return cartItems.size();
	}

	public boolean isEmpty() {
		return cartItems.isEmpty();
	}

	public void clearCart() {
		cartItems.clear();
		notifyCartChange();
	}

	private Optional<SaleItem> findItemByProduct(Product product) {
		return cartItems.stream().filter(item -> item.getProductId() == product.getId()).findFirst();
	}

	public boolean containsProduct(Product product) {
		return findItemByProduct(product).isPresent();
	}

	public int getProductQuantityInCart(Product product) {
		Optional<SaleItem> itemContainer = findItemByProduct(product);

		if (itemContainer.isPresent())
			return itemContainer.get().getQuantity();
		else
			return 0;
	}
}
