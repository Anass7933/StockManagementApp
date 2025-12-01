package com.stockapp.utils;

import static org.junit.jupiter.api.Assertions.*;
import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.models.enums.Category;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CartManagerTest {
	private CartManager cartManager;
	private ProductService productService;
	private Product realProduct;

	@BeforeEach
	void setUp() {
		productService = new ProductServiceImpl();
		cartManager = CartManager.getInstance();
		cartManager.clearCart();
		Product newProduct = new Product("CartManager Integration Product",
				"Created by Service",
				new BigDecimal("20.00"),
				50,
				5,
				Category.ELECTRONICS);
		this.realProduct = productService.create(newProduct);
	}

	@AfterEach
	void tearDown() {
		cartManager.clearCart();
		if (realProduct != null && realProduct.getId() > 0) {
			productService.delete(realProduct.getId());
		}
	}

	private Product createDummyProduct(long id, double price, int stock) {
		return new Product(id,
				"Dummy Product " + id,
				"Description",
				BigDecimal.valueOf(price),
				stock,
				5,
				OffsetDateTime.now(),
				Category.ELECTRONICS);
	}

	@Test
	void testAddItem_NewItem() {
		System.out.println("Running: testAddItem_NewItem");
		cartManager.addItem(realProduct, 5);
		assertEquals(1, cartManager.getTotalItemCount());
		assertEquals(100.0, cartManager.getTotalPrice());
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
		Product p1 = createDummyProduct(1001L, 10.0, 100);
		Product p2 = createDummyProduct(1002L, 5.0, 100);
		cartManager.addItem(p1, 2);
		cartManager.addItem(p2, 3);
		assertEquals(35.0, cartManager.getTotalPrice());
	}

	@Test
	void testUpdateItemQuantity_Success() {
		System.out.println("Running: testUpdateItemQuantity_Success");
		cartManager.addItem(realProduct, 1);
		SaleItem item = cartManager.getCartItems().get(0);
		cartManager.updateItemQuantity(item, 10);
		assertEquals(10, item.getQuantity());
		assertEquals(200.0, cartManager.getTotalPrice());
	}

	@Test
	void testUpdateItemQuantity_ExceedsRealStock() {
		System.out.println("Running: testUpdateItemQuantity_ExceedsRealStock");
		cartManager.addItem(realProduct, 1);
		SaleItem item = cartManager.getCartItems().get(0);
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
