package com.stockapp.utils;

import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.models.enums.Category;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CartManagerTest {

	private CartManager cartManager;
	private ProductService productService;
	private Product realProduct; // The product created in the DB

	@BeforeEach
	void setUp() {
		// 1. Initialize Service
		productService = new ProductServiceImpl();
		cartManager = CartManager.getInstance();

		// 2. Clear Cart (Singleton safety)
		cartManager.clearCart();

		// 3. Create a Real Product in DB using the Service
		// This replaces the complex 'createRealProductInDb' method
		Product newProduct = new Product(
				"CartManager Integration Product",
				"Created by Service",
				new BigDecimal("20.00"),
				50, // Quantity
				5, // Min Stock
				Category.ELECTRONICS);

		// The service handles SQL, ID generation, and returning the object
		this.realProduct = productService.create(newProduct);
	}

	@AfterEach
	void tearDown() {
		cartManager.clearCart();

		// Use the service to clean up
		if (realProduct != null && realProduct.getId() > 0) {
			productService.delete(realProduct.getId());
		}
	}

	// --- Helper for Dummy Objects (still useful for simple logic tests) ---
	private Product createDummyProduct(long id, double price, int stock) {
		return new Product(
				id,
				"Dummy Product " + id,
				"Description",
				BigDecimal.valueOf(price),
				stock,
				5,
				OffsetDateTime.now(),
				Category.ELECTRONICS);
	}

	// ================= TESTS =================

	@Test
	void testAddItem_NewItem() {
		System.out.println("Running: testAddItem_NewItem");

		// We can use the Real Product for this simple test too
		cartManager.addItem(realProduct, 5);

		assertEquals(1, cartManager.getTotalItemCount());
		assertEquals(100.0, cartManager.getTotalPrice()); // 5 * 20.00

		SaleItem item = cartManager.getCartItems().get(0);
		assertEquals(realProduct.getId(), item.getProductId());
	}

	@Test
	void testAddItem_ExistingItem_IncreasesQuantity() {
		System.out.println("Running: testAddItem_ExistingItem");

		cartManager.addItem(realProduct, 2);
		cartManager.addItem(realProduct, 3);

		assertEquals(1, cartManager.getTotalItemCount());
		assertEquals(5, cartManager.getCartItems().get(0).getQuantity());
		assertEquals(100.0, cartManager.getTotalPrice());
	}

	@Test
	void testAddItem_ExceedsStock_ThrowsException() {
		System.out.println("Running: testAddItem_ExceedsStock");

		// Real product has 50 stock. Try to add 51.
		assertThrows(IllegalArgumentException.class, () -> {
			cartManager.addItem(realProduct, 51);
		});
	}

	@Test
	void testRemoveItem() {
		System.out.println("Running: testRemoveItem");

		cartManager.addItem(realProduct, 1);
		SaleItem item = cartManager.getCartItems().get(0);

		cartManager.removeItem(item);

		assertTrue(cartManager.isEmpty());
	}

	@Test
	void testCalculateTotal_MultipleItems() {
		System.out.println("Running: testCalculateTotal");

		// Use Dummy products for this one to avoid creating 2 DB entries
		Product p1 = createDummyProduct(1001L, 10.0, 100);
		Product p2 = createDummyProduct(1002L, 5.0, 100);

		cartManager.addItem(p1, 2); // 20.0
		cartManager.addItem(p2, 3); // 15.0

		assertEquals(35.0, cartManager.getTotalPrice());
	}

	@Test
	void testUpdateItemQuantity_Success() {
		System.out.println("Running: testUpdateItemQuantity_Success");

		cartManager.addItem(realProduct, 1);
		SaleItem item = cartManager.getCartItems().get(0);

		// This works because realProduct exists in the DB
		cartManager.updateItemQuantity(item, 10);

		assertEquals(10, item.getQuantity());
		assertEquals(200.0, cartManager.getTotalPrice());
	}

	@Test
	void testUpdateItemQuantity_ExceedsRealStock() {
		System.out.println("Running: testUpdateItemQuantity_ExceedsRealStock");

		cartManager.addItem(realProduct, 1);
		SaleItem item = cartManager.getCartItems().get(0);

		// Try to update to 51 (Real DB stock is 50)
		assertThrows(IllegalArgumentException.class, () -> {
			cartManager.updateItemQuantity(item, 51);
		});
	}

	@Test
	void testClearCart() {
		System.out.println("Running: testClearCart");

		cartManager.addItem(realProduct, 1);
		cartManager.clearCart();

		assertTrue(cartManager.isEmpty());
	}
}
