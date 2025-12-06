package com.stockapp.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import com.stockapp.models.entities.Product;
import com.stockapp.models.enums.Category;

import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProductServiceImplTest {

	private static ProductServiceImpl productService;
	private static Product sharedProduct;

	private static final String SHARED_NAME = "JUnit Shared Fixture";
	private static final String TEMP_NAME = "temp_delete_prod";

	private static final int INITIAL_STOCK = 50;
	private static final int MIN_STOCK = 10;

	@BeforeAll
	static void setUp() {
		productService = new ProductServiceImpl();
		System.out.println("--- Setup: Ensuring clean environment ---");

		deleteIfExists(SHARED_NAME);
		deleteIfExists(TEMP_NAME);

		Product p = new Product(SHARED_NAME, "Shared Desc", new BigDecimal("100.00"), INITIAL_STOCK, MIN_STOCK,
				Category.ELECTRONICS);
		sharedProduct = productService.create(p);

		System.out.println("Shared Product created with ID: " + sharedProduct.getId());
	}

	private static void deleteIfExists(String productName) {
		try {
			Optional<Product> old = productService.findByName(productName);
			if (old.isPresent()) {
				productService.delete(old.get().getId());
				System.out.println("Removed stale test product: " + productName);
			}
		} catch (Exception e) {
			System.err.println("Warning: Cleanup failed for " + productName);
		}
	}

	@AfterAll
	static void tearDown() {
		System.out.println("--- Teardown: Deleting Shared Product ---");
		deleteIfExists(SHARED_NAME);
		deleteIfExists(TEMP_NAME);
	}

	@Test
	void testReadById_SharedProduct() {
		System.out.println("Running: testReadById_SharedProduct");

		Optional<Product> fetched = assertDoesNotThrow(() -> {
			return productService.read(sharedProduct.getId());
		}, "Critical Failure : the function read threw an unexpected exception");

		assertTrue(fetched.isPresent(), "Should find the shared product");
		assertEquals(SHARED_NAME, fetched.get().getName());
		assertEquals(Category.ELECTRONICS, fetched.get().getCategory());
	}

	@Test
	void testReadByName_SharedProduct() {
		System.out.println("Ruunning: testReadByName_SharedProduct");

		Optional<Product> fetched = assertDoesNotThrow(() -> {
			return productService.findByName(sharedProduct.getName());
		}, "Critical Failure : the function findByName threw an unexpected exception");

		assertTrue(fetched.isPresent(), "Should find the shared product");
		assertEquals(SHARED_NAME, fetched.get().getName());
		assertEquals(Category.ELECTRONICS, fetched.get().getCategory());
	}

	@Test
	void testUpdateStock_Success() {
		System.out.println("Running: testUpdateStock_Success");

		Product currentP = productService.read(sharedProduct.getId()).orElseThrow();
		int oldQty = currentP.getQuantity();
		int amountToAdd = 5;

		assertDoesNotThrow(() -> {
			productService.updateStock(sharedProduct.getId(), amountToAdd);
		}, "Critical Failure : the function updateStock threw an unexpected exception");

		Product updatedP = productService.read(sharedProduct.getId()).orElseThrow();
		assertEquals(oldQty + amountToAdd, updatedP.getQuantity(), "Stock should increase by exactly " + amountToAdd);
	}

	@Test
	void testUpdateStock_NotEnoughStock() {
		System.out.println("Running: testUpdateStock_NotEnoughStock");

		Product currentP = productService.read(sharedProduct.getId()).orElseThrow();
		int currentQty = currentP.getQuantity();

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			productService.updateStock(sharedProduct.getId(), -(currentQty + 1));
		}, "Critical Failure: The system allowed updating stock below zero! (No exception was thrown)");

		if (exception.getCause() instanceof java.sql.SQLException)
			throw exception;

		assertTrue(exception.getMessage().contains("Not enough stock"),
				"Expected 'Not enough stock' error, but got: " + exception.getMessage());
	}

	@Test
	void testIsNeedRestock() {
		System.out.println("Running: testIsNeedRestock");

		Product p = productService.read(sharedProduct.getId()).orElseThrow();
		int currentQty = p.getQuantity();

		int targetLow = MIN_STOCK - 1;
		if (currentQty > targetLow) {
			productService.updateStock(sharedProduct.getId(), -(currentQty - targetLow));
		}

		boolean isRestockNeeded = assertDoesNotThrow(() -> {
			return productService.isNeedRestock(sharedProduct.getId());
		}, "Critical Failure: isNeedRestock threw an unexpected exception");

		assertTrue(isRestockNeeded, "Should return TRUE when stock is below min");

		productService.updateStock(sharedProduct.getId(), 10);

		boolean isRestockStillNeeded = assertDoesNotThrow(() -> {
			return productService.isNeedRestock(sharedProduct.getId());
		}, "Critical Failure: isNeedRestock threw an unexpected exception");

		assertFalse(isRestockStillNeeded, "Should return FALSE when stock is sufficient");
	}

	@Test
	void testDeleteProduct_Independent() {
		System.out.println("Running: testDeleteProduct_Independent");

		Product temp = new Product(TEMP_NAME, "Disposable", new BigDecimal("1.00"), 1, 1, Category.TOYS);
		Product createdTemp = productService.create(temp);

		assertNotNull(createdTemp.getId(), "Temp product should be created");

		assertDoesNotThrow(() -> {
			productService.delete(createdTemp.getId());
		}, "Critical Failure : delete thew an unexpected exception");

		Optional<Product> deleted = productService.read(createdTemp.getId());
		assertFalse(deleted.isPresent(), "Temp product should be deleted");
	}

	@Test
	void testFindByPrefixName() {
		System.out.println("Running: testFindByPrefixName");

		List<Product> products = assertDoesNotThrow(() -> {
			return productService.findByPreName("JUnit");
		}, "findByPrefixName should not throw an exception");

		assertNotNull(products, "Returned list should not be null");
		assertFalse(products.isEmpty(), "Should find at least one product with the prefix");
		assertTrue(products.stream().anyMatch(p -> p.getId() == sharedProduct.getId()),
				"Shared product should be found by its prefix");
	}
}
