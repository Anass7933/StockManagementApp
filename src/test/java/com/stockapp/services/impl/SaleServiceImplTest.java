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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaleServiceImplTest {

	private SaleService saleService;
	private ProductService productService;
	private SaleItemService saleItemService;

	// We store the IDs of created objects to clean them up in tearDown()
	private Long createdProductId;
	private Long createdSaleId;

	@BeforeEach
	void setUp() {
		saleService = new SaleServiceImpl();
		productService = new ProductServiceImpl();
		saleItemService = new SaleItemServiceImpl();

		// 1. Create a Product using the Service (No raw SQL!)
		Product p = new Product(
				"Sale Service Test Product",
				"Testing sales integration",
				new BigDecimal("50.00"), // Price
				100, // Initial Quantity
				10, // Min Stock
				Category.ELECTRONICS);

		// This will insert into DB and set the ID on the object
		Product savedProduct = productService.create(p);
		this.createdProductId = savedProduct.getId();
	}

	@AfterEach
	void tearDown() {
		// Clean up in the reverse order of creation (Items -> Sale -> Product)

		// 1. Delete Sale and its Items
		if (createdSaleId != null) {
			// First, delete items linked to this sale (using SaleItemService)
			// Note: SaleItemService.delete(saleId) deletes all items for that sale
			try {
				saleItemService.delete(createdSaleId);
			} catch (Exception e) {
				System.out.println("Warning: Could not delete sale items: " + e.getMessage());
			}

			// Second, delete the sale itself
			try {
				saleService.delete(createdSaleId);
			} catch (Exception e) {
				System.out.println("Warning: Could not delete sale: " + e.getMessage());
			}
		}

		// 2. Delete the Product
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

		// 1. Prepare Data
		Sale sale = new Sale(100L); // Total price placeholder

		// Create 1 Item buying 2 units of our test product
		List<SaleItem> items = new ArrayList<>();
		SaleItem item = new SaleItem();
		item.setProductId(createdProductId); // Use the real ID from setUp()
		item.setQuantity(2);
		item.setUnitPrice(50.00);
		items.add(item);

		// 2. Execute Logic (This is the method under test)
		Sale createdSale = ((SaleServiceImpl) saleService).createSaleWithItems(sale, items);

		// Store ID for cleanup
		this.createdSaleId = createdSale.getId();

		// 3. Verify Sale Header
		assertNotNull(createdSale.getId(), "Sale ID should be generated");
		assertEquals(100L, createdSale.getTotalPrice());

		// 4. Verify Sale Items in DB
		List<SaleItem> savedItems = saleItemService.findBySaleId(createdSale.getId());
		assertEquals(1, savedItems.size(), "Should have saved 1 sale item");
		assertEquals(createdProductId, savedItems.get(0).getProductId(), "Saved item should match product ID");

		// 5. Verify Inventory Update (The Critical Check)
		// Initial Stock was 100. Bought 2. Expect 98.
		Product updatedProduct = productService.read(createdProductId).orElseThrow();
		assertEquals(98, updatedProduct.getQuantity(), "Stock should decrease by 2");
	}

	@Test
	void testCreateSale_Simple() {
		System.out.println("Running: testCreateSale_Simple");

		Sale sale = new Sale(500L);
		Sale created = saleService.create(sale);

		this.createdSaleId = created.getId(); // Mark for cleanup

		assertNotNull(created.getId());
		assertEquals(500L, created.getTotalPrice());
	}

	@Test
	void testGetTotalRevenue() {
		System.out.println("Running: testGetTotalRevenue");

		Sale sale = new Sale(250L);
		Sale created = saleService.create(sale);
		this.createdSaleId = created.getId(); // Mark for cleanup

		Long revenue = saleService.getTotalRevenue(created.getId());
		assertEquals(250L, revenue);
	}

	@Test
	void testCreateSale_InsufficientStock() {
		System.out.println("Running: testCreateSale_InsufficientStock");

		Sale sale = new Sale(5000L);

		// Try to buy 101 units (Stock is 100)
		List<SaleItem> items = new ArrayList<>();
		SaleItem item = new SaleItem();
		item.setProductId(createdProductId);
		item.setQuantity(101);
		item.setUnitPrice(50.00);
		items.add(item);

		// Expect RuntimeException from ProductService
		Exception exception = assertThrows(RuntimeException.class, () -> {
			((SaleServiceImpl) saleService).createSaleWithItems(sale, items);
		});

		// Note: In this case, no sale is created in DB due to rollback/exception,
		// so createdSaleId remains null, and tearDown handles that gracefully.
	}
}
