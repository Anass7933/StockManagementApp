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
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaleServiceImplTest {
	private SaleService saleService;
	private ProductService productService;
	private SaleItemService saleItemService;
	private Long createdProductId;
	private Long createdSaleId;

	@BeforeEach
	void setUp() {
		saleService = new SaleServiceImpl();
		productService = new ProductServiceImpl();
		saleItemService = new SaleItemServiceImpl();
		Product p = new Product("Sale Service Test Product",
				"Testing sales integration",
				new BigDecimal("50.00"),
				100,
				10,
				Category.ELECTRONICS);
		Product savedProduct = productService.create(p);
		this.createdProductId = savedProduct.getId();
	}

	@AfterEach
	void tearDown() {
		if (createdSaleId != null) {
			try {
				saleItemService.delete(createdSaleId);
			} catch (Exception e) {
				System.out.println("Warning: Could not delete sale items: " + e.getMessage());
			}
			try {
				saleService.delete(createdSaleId);
			} catch (Exception e) {
				System.out.println("Warning: Could not delete sale: " + e.getMessage());
			}
		}
		if (createdProductId != null) {
			try {
				productService.delete(createdProductId);
			} catch (Exception e) {
				System.out.println("Warning: Could not delete product: " + e.getMessage());
			}
		}
	}

	@Test
	void testCreateSaleWithItems_Success() {
		System.out.println("Running: testCreateSaleWithItems_Success");
		Sale sale = new Sale(100L);
		List<SaleItem> items = new ArrayList<>();
		SaleItem item = new SaleItem();
		item.setProductId(createdProductId);
		item.setQuantity(2);
		item.setUnitPrice(50.00);
		items.add(item);
		Sale createdSale = ((SaleServiceImpl) saleService).createSaleWithItems(sale, items);
		this.createdSaleId = createdSale.getId();
		assertNotNull(createdSale.getId(), "Sale ID should be generated");
		assertEquals(100L, createdSale.getTotalPrice());
		List<SaleItem> savedItems = saleItemService.findBySaleId(createdSale.getId());
		assertEquals(1, savedItems.size(), "Should have saved 1 sale item");
		assertEquals(createdProductId, savedItems.get(0).getProductId(), "Saved item should match product ID");
		Product updatedProduct = productService.read(createdProductId).orElseThrow();
		assertEquals(98, updatedProduct.getQuantity(), "Stock should decrease by 2");
	}

	@Test
	void testCreateSale_Simple() {
		System.out.println("Running: testCreateSale_Simple");
		Sale sale = new Sale(500L);
		Sale created = saleService.create(sale);
		this.createdSaleId = created.getId();
		assertNotNull(created.getId());
		assertEquals(500L, created.getTotalPrice());
	}

	@Test
	void testGetTotalRevenue() {
		System.out.println("Running: testGetTotalRevenue");
		Sale sale = new Sale(250L);
		Sale created = saleService.create(sale);
		this.createdSaleId = created.getId();
		Long revenue = saleService.getTotalRevenue(created.getId());
		assertEquals(250L, revenue);
	}

	@Test
	void testCreateSale_InsufficientStock() {
		System.out.println("Running: testCreateSale_InsufficientStock");
		Sale sale = new Sale(5000L);
		List<SaleItem> items = new ArrayList<>();
		SaleItem item = new SaleItem();
		item.setProductId(createdProductId);
		item.setQuantity(101);
		item.setUnitPrice(50.00);
		items.add(item);
		Exception exception = assertThrows(RuntimeException.class,
				() -> {
					((SaleServiceImpl) saleService).createSaleWithItems(sale, items);
				});
	}
}
