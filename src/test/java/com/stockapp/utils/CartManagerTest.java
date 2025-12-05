package com.stockapp.utils;

import com.stockapp.models.entities.Product;
import com.stockapp.models.enums.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartManagerTest {

	private CartManager cartManager;

	@BeforeEach
	public void setUp() {
		// Reset singleton for testing (hacky but needed since it's a singleton)
		// Ideally CartManager shouldn't be a singleton or should have a reset method
		// For now, we'll just create a new instance via reflection or just assume we
		// can get a fresh one if we could
		// But since it's a singleton, we might be sharing state.
		// Let's rely on clearCart() for now.
		cartManager = CartManager.getInstance();
		cartManager.clearCart();
		// Clear listeners if possible? No method for that.
		// We can't easily clear listeners without reflection or adding a method.
		// Let's just add a method to clear listeners for testing or just rely on the
		// fact that we are adding new ones.
	}

	@Test
	public void testMultipleListeners() {
		AtomicInteger listener1Count = new AtomicInteger(0);
		AtomicInteger listener2Count = new AtomicInteger(0);

		Runnable listener1 = listener1Count::incrementAndGet;
		Runnable listener2 = listener2Count::incrementAndGet;

		cartManager.addCartChangeListener(listener1);
		cartManager.addCartChangeListener(listener2);

		Product product = new Product(1L, "Test", "Desc", BigDecimal.TEN, 10, 1, null, Category.BOOKS);
		cartManager.addItem(product, 1);

		assertEquals(1, listener1Count.get(), "Listener 1 should be called once");
		assertEquals(1, listener2Count.get(), "Listener 2 should be called once");

		cartManager.removeCartChangeListener(listener1);
		cartManager.addItem(product, 1);

		assertEquals(1, listener1Count.get(), "Listener 1 should not be called again");
		assertEquals(2, listener2Count.get(), "Listener 2 should be called again");

		// Cleanup
		cartManager.removeCartChangeListener(listener2);
	}
}
