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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaleItemServiceImplTest {
	private SaleItemService saleItemService;
	private SaleService saleService;
	private ProductService productService;
	private Long testSaleId;
	private Long testProductId;

	@BeforeEach
	void setUp() {
		saleItemService = new SaleItemServiceImpl();
		saleService = new SaleServiceImpl();
		productService = new ProductServiceImpl();
		Product p = new Product("Item Test Product", "Desc", new BigDecimal("10.00"), 100, 5, Category.FASHION);
		testProductId = productService.create(p).getId();
		Sale s = new Sale(0L);
		testSaleId = saleService.create(s).getId();
	}

	@AfterEach
	void tearDown() {
		if (testSaleId != null) {
			saleItemService.delete(testSaleId);
			saleService.delete(testSaleId);
		}
		if (testProductId != null) {
			productService.delete(testProductId);
		}
	}

	@Test
	void testCreateAndReadItem() {
		System.out.println("Running: testCreateAndReadItem");
		SaleItem item = new SaleItem();
		item.setSaleId(testSaleId);
		item.setProductId(testProductId);
		item.setQuantity(5);
		item.setUnitPrice(10.00);
		SaleItem created = saleItemService.create(item);
		assertNotNull(created.getId());
		assertEquals(50.00, created.getLineTotal(), "Line total should be 5 * 10.00");
		Optional<SaleItem> fetched = saleItemService.read(created.getId());
		assertTrue(fetched.isPresent());
		assertEquals(5, fetched.get().getQuantity());
	}

	@Test
	void testUpdateItem() {
		System.out.println("Running: testUpdateItem");
		SaleItem item = new SaleItem(testProductId, testSaleId, 2, 10.00);
		item = saleItemService.create(item);
		item.setQuantity(3);
		SaleItem updated = saleItemService.update(item);
		assertEquals(3, updated.getQuantity());
		assertEquals(30.00, updated.getLineTotal(), "Line total should update to 3 * 10.00");
	}

	@Test
	void testFindByProductId() {
		System.out.println("Running: testFindByProductId");
		saleItemService.create(new SaleItem(testProductId, testSaleId, 1, 10.00));
		saleItemService.create(new SaleItem(testProductId, testSaleId, 2, 10.00));
		List<SaleItem> items = saleItemService.findByProductId(testProductId);
		assertTrue(items.size() >= 2, "Should find at least 2 items for this product");
	}

	@Test
	void testDeleteBySaleId() {
		System.out.println("Running: testDeleteBySaleId");
		SaleItem item = saleItemService.create(new SaleItem(testProductId, testSaleId, 1, 10.00));
		saleItemService.delete(testSaleId);
		Optional<SaleItem> result = saleItemService.read(item.getId());
		assertFalse(result.isPresent(), "Item should be deleted");
	}
}
