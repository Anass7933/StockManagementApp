package com.stockapp.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.Sale;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.models.enums.Category;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SaleItemServiceImplTest {

	private static SaleItemServiceImpl saleItemService;
	private static SaleServiceImpl saleService;
	private static ProductServiceImpl productService;

	private static SaleItem sharedSaleItem;
	private static Sale sharedSale;
	private static Product sharedProduct;

	private static final String SHARED_PROD_NAME = "JUnit SaleItem Product";

	@BeforeAll
	static void setUp() {
		saleItemService = new SaleItemServiceImpl();
		saleService = new SaleServiceImpl();
		productService = new ProductServiceImpl();

		System.out.println("--- Setup: ensuring environment is clean ---");

		Product p = new Product(SHARED_PROD_NAME, "Desc", new BigDecimal("20.00"), 100, 10, Category.ELECTRONICS);
		sharedProduct = productService.create(p);

		Sale s = new Sale(0);
		sharedSale = saleService.create(s);

		SaleItem item = new SaleItem(
				sharedProduct.getId(),
				sharedSale.getId(),
				5,
				20.00);

		sharedSaleItem = saleItemService.create(item);
		System.out.println("Shared SaleItem created with ID: " + sharedSaleItem.getId());
	}

	@AfterAll
	static void tearDown() {
		System.out.println("--- Teardown: Recursive Cleanup ---");
		System.out.println("cleaning any remaining data");
		try {
			if (sharedSaleItem != null) {
				saleItemService.delete(sharedSaleItem.getId());
			}
			if (sharedSale != null) {
				saleService.delete(sharedSale.getId());
			}
			if (sharedProduct != null) {
				productService.delete(sharedProduct.getId());
			}
			System.out.println("Cleanup complete.");
		} catch (Exception e) {
			System.err.println("WARNING: Hierarchy cleanup failed.");
			e.printStackTrace();
		}
	}

	@Test
	void testRead_SharedItem() {
		System.out.println("Running: testRead_SharedItem");

		Optional<SaleItem> fetched = assertDoesNotThrow(() -> {
			return saleItemService.read(sharedSaleItem.getId());
		}, "Critical Failure : read threw an unexpected exception");

		assertTrue(fetched.isPresent(), "Should find the shared item");
		assertEquals(sharedProduct.getId(), fetched.get().getProductId());
		assertEquals(100.00, fetched.get().getLineTotal(), 0.001);
	}

	@Test
	void testUpdate_Quantity() {
		System.out.println("Running: testUpdate_Quantity");

		SaleItem current = saleItemService.read(sharedSaleItem.getId()).orElseThrow();

		current.setQuantity(2);

		assertDoesNotThrow(() -> saleItemService.update(current),
				"Critical Failure : update threw an unexpected exception");

		SaleItem updated = saleItemService.read(sharedSaleItem.getId()).orElseThrow();

		assertEquals(2, updated.getQuantity());
		assertEquals(40.00, updated.getLineTotal(), 0.001);

		System.out.println("	undoing changes");
		try {
			current.setQuantity(5);
			saleItemService.update(current);
		} catch (Exception e) {
			throw new RuntimeException("	error undoing changes", e);
		}
	}

	@Test
	void testDelete_Independent() {
		System.out.println("Running: testDelete_Independent");

		SaleItem tempItem = new SaleItem(
				sharedProduct.getId(),
				sharedSale.getId(),
				1,
				50.00);

		SaleItem created = saleItemService.create(tempItem);
		assertNotNull(created.getId());

		assertDoesNotThrow(() -> saleItemService.delete(created.getId()),
				"Critical Failure : delete threw an unexpected exception");

		Optional<SaleItem> deleted = saleItemService.read(created.getId());
		assertFalse(deleted.isPresent());
	}

	@Test
	void testFindByProductId() {
		System.out.println("Running: testFindByProductId");

		List<SaleItem> items = assertDoesNotThrow(() -> {
			return saleItemService.findByProductId(sharedProduct.getId());
		}, "Critical Failure : : findByProductId threw an unexpected exception");

		assertFalse(items.isEmpty(), "Product should have at least one sale item (the shared one)");

		boolean found = items.stream().anyMatch(i -> i.getId() == sharedSaleItem.getId());
		assertTrue(found, "List should contain our shared item");
	}

	@Test
	void testFindBySaleId() {
		System.out.println("Running: testFindBySaleId");

		List<SaleItem> items = assertDoesNotThrow(() -> {
			return saleItemService.findBySaleId(sharedSale.getId());
		}, "Critical Failure : findBySaleId threw an unexpected exception");

		assertFalse(items.isEmpty(), "Sale should have items");

		boolean found = items.stream().anyMatch(i -> i.getId() == sharedSaleItem.getId());
		assertTrue(found, "List should contain our shared item");
	}
}
