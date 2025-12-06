package com.stockapp.services.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.Sale;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.models.enums.Category;
import com.stockapp.services.interfaces.ProductService;
import com.stockapp.services.interfaces.SaleItemService;
import com.stockapp.services.interfaces.SaleService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SaleServiceImplTest {

	private static SaleService saleService;
	private static SaleItemService saleItemService;
	private static ProductService productService;
	private static Sale sharedSale;

	private static final long SHARED_TOTAL_PRICE = 150;

	@BeforeAll
	static void setUp() {
		saleService = new SaleServiceImpl();
		saleItemService = new SaleItemServiceImpl();
		productService = new ProductServiceImpl();
		System.out.println("--- Setup: ensuring clean environment---");

		Sale s = new Sale(BigDecimal.valueOf(SHARED_TOTAL_PRICE));

		sharedSale = saleService.create(s);
		System.out.println("Shared Sale created with ID: " + sharedSale.getId());
	}

	@AfterAll
	static void tearDown() {
		System.out.println("--- Teardown: Cleaning up ---");
		try {
			if (sharedSale != null) {

				if (saleService.read(sharedSale.getId()).isPresent()) {
					saleService.delete(sharedSale.getId());
					System.out.println("Shared sale deleted.");
				}
			}
		} catch (Exception e) {
			System.err.println("WARNING: Cleanup failed.");
			e.printStackTrace();
		}
	}

	@Test
	void testRead_SharedSale() {

		System.out.println("Running: testRead_SharedSale");

		Optional<Sale> fetched = assertDoesNotThrow(() -> {
			return saleService.read(sharedSale.getId());
		}, "Critical Failure : read threw an unexpected exception");

		assertTrue(fetched.isPresent(), "Should find the shared sale");
		assertEquals(SHARED_TOTAL_PRICE, fetched.get().getTotalPrice().doubleValue(), 0.001);
		assertNotNull(fetched.get().getCreatedAt(), "Date should be auto-generated");
	}

	@Test
	void testUpdate_Sale() {
		System.out.println("Running: testUpdate_Sale");

		Sale current = saleService.read(sharedSale.getId()).orElseThrow();
		BigDecimal oldPrice = current.getTotalPrice();
		BigDecimal newPrice = BigDecimal.valueOf(999);

		current.setTotalPrice(newPrice);

		assertDoesNotThrow(() -> saleService.update(current),
				"Critical Failure : update threw an unexpected exception");

		Sale updated = saleService.read(sharedSale.getId()).orElseThrow();
		assertEquals(newPrice.doubleValue(), updated.getTotalPrice().doubleValue(), 0.001);

		System.out.println("    undoing changes");
		try {
			current.setTotalPrice(oldPrice);
			saleService.update(current);
		} catch (Exception e) {
			throw new RuntimeException("    Error undoing changes", e);
		}
	}

	@Test
	void testDelete_Independent() {
		System.out.println("Running: testDelete_Independent");

		Sale tempSale = new Sale(BigDecimal.valueOf(10));

		Sale created = saleService.create(tempSale);
		assertNotNull(created.getId());

		assertDoesNotThrow(() -> saleService.delete(created.getId()),
				"Critical Failure : delete threw an unexpected exception");

		Optional<Sale> deleted = saleService.read(created.getId());
		assertFalse(deleted.isPresent(), "Sale should be gone");
	}

	@Test
	void testTotalRevenue() {
		System.out.println("Running: testTotalRevenue");
		SaleServiceImpl saleServiceImpl = new SaleServiceImpl();
		java.time.LocalDate endDate = java.time.LocalDate.now();
		java.time.LocalDate startDate = endDate.minusDays(30);

		int revenue = assertDoesNotThrow(() -> {
			return saleServiceImpl.totalRevenue(startDate, endDate);
		}, "Critical Failure : totalRevenue threw an unexpected exception");

		assertTrue(revenue >= 0, "Revenue should be non-negative");
	}

	@Test
	void testCreateSaleWithItems_Transaction() {
		System.out.println("Running: testCreateSaleWithItems_Transaction");

		Product tempProd = new Product("Txn Test Product", "Desc", new BigDecimal("10.00"), 20, 5, Category.TOYS);
		Product savedProd = productService.create(tempProd);

		Sale createdSale = null;

		try {

			Sale saleHeader = new Sale(BigDecimal.valueOf(20));

			SaleItem item = new SaleItem(
					savedProd.getId(),
					saleHeader.getId(),
					2,
					BigDecimal.valueOf(10.00));

			createdSale = assertDoesNotThrow(() -> {
				return saleService.createSaleWithItems(saleHeader, List.of(item));
			}, "Critical Failure : createSaleWithItems threw an unexpected exception");

			assertNotNull(createdSale.getId());

			List<SaleItem> items = saleItemService.findBySaleId(createdSale.getId());
			assertEquals(1, items.size(), "Should have saved 1 sale item");
			assertEquals(savedProd.getId(), items.get(0).getProductId());

			Product updatedProd = productService.read(savedProd.getId()).orElseThrow();
			assertEquals(18, updatedProd.getQuantity(), "Stock should decrease by the sold amount");

		} finally {

			System.out.println("    Cleaning up transaction test data");

			if (createdSale != null) {
				try {
					saleService.delete(createdSale.getId());
				} catch (Exception e) {
				}
			}

			try {
				productService.delete(savedProd.getId());
			} catch (Exception e) {
			}
		}
	}
}
