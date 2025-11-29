package com.stockapp.services.impl;

import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.Sale;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.models.enums.Category;
import com.stockapp.services.interfaces.ProductService;
import com.stockapp.services.interfaces.SaleItemService;
import com.stockapp.services.interfaces.SaleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SaleItemServiceImplTest {

	private SaleItemService saleItemService;
	private SaleService saleService;
	private ProductService productService;

	// Foreign Keys needed for every test
	private Long testSaleId;
	private Long testProductId;

	@BeforeEach
	void setUp() {
		saleItemService = new SaleItemServiceImpl();
		saleService = new SaleServiceImpl();
		productService = new ProductServiceImpl();

		// 1. Create a Helper Product
		Product p = new Product("Item Test Product", "Desc", new BigDecimal("10.00"), 100, 5, Category.FASHION);
		testProductId = productService.create(p).getId();

		// 2. Create a Helper Sale
		Sale s = new Sale(0L); // Price doesn't matter for this test
		testSaleId = saleService.create(s).getId();
	}

	@AfterEach
	void tearDown() {
		// Cleanup Child (Items) -> Parent (Sale) -> Parent (Product)
		if (testSaleId != null) {
			saleItemService.delete(testSaleId); // Helper to clear items
			saleService.delete(testSaleId);
		}
		if (testProductId != null) {
			productService.delete(testProductId);
		}
	}

	@Test
	void testCreateAndReadItem() {
		System.out.println("Running: testCreateAndReadItem");

		// 1. Create Item
		SaleItem item = new SaleItem();
		item.setSaleId(testSaleId);
		item.setProductId(testProductId);
		item.setQuantity(5);
		item.setUnitPrice(10.00);
		// Note: Line total is usually calculated by DB trigger or logic,
		// but your service returns the object updated from DB RETURNING clause.

		SaleItem created = saleItemService.create(item);

		// 2. Assert Creation
		assertNotNull(created.getId());
		assertEquals(50.00, created.getLineTotal(), "Line total should be 5 * 10.00");

		// 3. Read specific item
		Optional<SaleItem> fetched = saleItemService.read(created.getId());
		assertTrue(fetched.isPresent());
		assertEquals(5, fetched.get().getQuantity());
	}

	@Test
	void testUpdateItem() {
		System.out.println("Running: testUpdateItem");

		// 1. Create initial item
		SaleItem item = new SaleItem(testProductId, testSaleId, 2, 10.00);
		item = saleItemService.create(item);

		// 2. Modify it (e.g. user made a mistake, quantity was actually 3)
		item.setQuantity(3);

		// 3. Update in DB
		SaleItem updated = saleItemService.update(item);

		// 4. Verify Update
		assertEquals(3, updated.getQuantity());
		assertEquals(30.00, updated.getLineTotal(), "Line total should update to 3 * 10.00");
	}

	@Test
	void testFindByProductId() {
		System.out.println("Running: testFindByProductId");

		// 1. Create two items for the same product (in the same sale for simplicity)
		saleItemService.create(new SaleItem(testProductId, testSaleId, 1, 10.00));
		saleItemService.create(new SaleItem(testProductId, testSaleId, 2, 10.00));

		// 2. Find by Product
		List<SaleItem> items = saleItemService.findByProductId(testProductId);

		// 3. Assert
		assertTrue(items.size() >= 2, "Should find at least 2 items for this product");
	}

	@Test
	void testDeleteBySaleId() {
		System.out.println("Running: testDeleteBySaleId");

		// 1. Create item
		SaleItem item = saleItemService.create(new SaleItem(testProductId, testSaleId, 1, 10.00));

		// 2. Delete all items for this sale
		saleItemService.delete(testSaleId);

		// 3. Verify they are gone
		Optional<SaleItem> result = saleItemService.read(item.getId());
		assertFalse(result.isPresent(), "Item should be deleted");
	}
}
