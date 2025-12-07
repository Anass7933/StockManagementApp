package com.stockapp.utils;

import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.SaleItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartManagerTest {

	private static CartManager cartManager;
	private static Product productA;
	private static Product productB;

	@BeforeAll
	static void setUp() {
		System.out.println("--- Setup : ensuring environment is clean ---");

		cartManager = new CartManager();

		productA = new Product(
				1L,
				"Test Product A",
				"Description A",
				new BigDecimal("10.00"),
				100,
				10,
				OffsetDateTime.now(),
				null);

		productB = new Product(
				2L,
				"Test Product B",
				"Description B",
				new BigDecimal("5.50"),
				50,
				5,
				OffsetDateTime.now(),
				null);
	}

	@AfterAll
	static void tearDown() {
		System.out.println("All tests finished.");
		cartManager = null;
	}

	@Test
	@Order(1)
	void testSingletonInstance() {
		System.out.println("running : testSingletonInstance test");

		CartManager instance1 = CartManager.getInstance();
		CartManager instance2 = CartManager.getInstance();

		assertSame(instance1, instance2, "CartManager instances should be the same (Singleton Pattern failed)");
	}

	@Test
	@Order(2)
	void testAddItem() {
		System.out.println("running : testAddItem test");
		cartManager.clearCart();

		cartManager.addItem(productA, 5);

		assertEquals(1, cartManager.getTotalItemCount(), "Cart should have exactly 1 item type");
		assertEquals(5, cartManager.getProductQuantityInCart(productA), "Product A quantity should be 5");
		assertFalse(cartManager.isEmpty(), "Cart should not be empty after adding item");
	}

	@Test
	@Order(3)
	void testAddItemMerge() {
		System.out.println("running : testAddItemMerge test");
		cartManager.clearCart();

		cartManager.addItem(productA, 5);
		cartManager.addItem(productA, 3);

		assertEquals(1, cartManager.getTotalItemCount(), "Cart should merge duplicate products into 1 row");
		assertEquals(8, cartManager.getProductQuantityInCart(productA), "Total quantity should be 5 + 3 = 8");
	}

	@Test
	@Order(4)
	void testAddInvalidQuantity() {
		System.out.println("running : testAddInvalidQuantity test");
		cartManager.clearCart();

		assertThrows(IllegalArgumentException.class, () -> {
			cartManager.addItem(productA, 0);
		}, "Should throw exception when adding 0 quantity");

		assertThrows(IllegalArgumentException.class, () -> {
			cartManager.addItem(productA, -5);
		}, "Should throw exception when adding negative quantity");
	}

	@Test
	@Order(5)
	void testAddExceedingStock() {
		System.out.println("running : testAddExceedingStock test");
		cartManager.clearCart();

		assertThrows(IllegalArgumentException.class, () -> {
			cartManager.addItem(productA, 101);
		}, "Should throw exception when adding quantity > available stock");
	}

	@Test
	@Order(6)
	void testTotalPriceCalculation() {
		System.out.println("running : testTotalPriceCalculation test");
		cartManager.clearCart();

		cartManager.addItem(productA, 2);

		cartManager.addItem(productB, 4);

		BigDecimal expectedTotal = new BigDecimal("42.00");
		assertEquals(expectedTotal, cartManager.getTotalPrice(), "Total price calculation is incorrect");
	}

	@Test
	@Order(7)
	void testRemoveItem() {
		System.out.println("running : testRemoveItem test");
		cartManager.clearCart();

		cartManager.addItem(productA, 5);
		SaleItem item = cartManager.getCartItems().get(0);

		cartManager.removeItem(item);

		assertTrue(cartManager.isEmpty(), "Cart should be empty after removing the only item");
		assertEquals(0, cartManager.getTotalItemCount(), "Item count should be 0 after removal");
	}

	@Test
	@Order(8)
	void testClearCart() {
		System.out.println("running : testClearCart test");
		cartManager.clearCart();

		cartManager.addItem(productA, 1);
		cartManager.addItem(productB, 1);

		cartManager.clearCart();

		assertTrue(cartManager.isEmpty(), "Cart should be empty after clearCart()");
		assertEquals(BigDecimal.ZERO, cartManager.getTotalPrice(), "Total price should be zero after clearCart()");
	}

	@Test
	@Order(9)
	void testContainsProduct() {
		System.out.println("running : testContainsProduct test");
		cartManager.clearCart();

		cartManager.addItem(productA, 1);

		assertTrue(cartManager.containsProduct(productA), "Should return true for product inside cart");
		assertFalse(cartManager.containsProduct(productB), "Should return false for product not in cart");
	}
}
