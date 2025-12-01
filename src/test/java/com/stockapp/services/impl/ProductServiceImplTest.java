package com.stockapp.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import com.stockapp.models.entities.Product;
import com.stockapp.models.enums.Category;
import com.stockapp.utils.DatabaseUtils;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductServiceImplTest {
	private ProductServiceImpl productService;
	private final String TEST_PRODUCT_NAME = "JUnit Integration Test Product";

	@BeforeEach
	void setUp() throws SQLException {
		productService = new ProductServiceImpl();
		deleteTestProduct();
	}

	@AfterEach
	void tearDown() throws SQLException {
		deleteTestProduct();
	}

	private void deleteTestProduct() throws SQLException {
		try (Connection conn = DatabaseUtils.getConnection()) {
			String sql = "DELETE FROM products WHERE name = ?";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, TEST_PRODUCT_NAME);
				ps.executeUpdate();
			}
		}
	}

	@Test
	void testCreateAndReadProduct() {
		System.out.println("Running: testCreateAndReadProduct");
		Product newProduct = new Product(
				TEST_PRODUCT_NAME, "Description for test", new BigDecimal("99.99"), 10, 2, Category.ELECTRONICS);
		Product createdProduct = productService.create(newProduct);
		assertNotNull(createdProduct.getId(), "Product ID should be generated");
		assertNotNull(createdProduct.getCreatedAt(), "Created At should be generated");
		assertEquals(TEST_PRODUCT_NAME, createdProduct.getName());
		assertEquals(Category.ELECTRONICS, createdProduct.getCategory());
		Optional<Product> fetchedProduct = productService.read(createdProduct.getId());
		assertTrue(fetchedProduct.isPresent(), "Should be able to find product by ID");
		assertEquals(new BigDecimal("99.99"), fetchedProduct.get().getPrice());
	}

	@Test
	void testUpdateStock_Success() {
		System.out.println("Running: testUpdateStock_Success");
		Product p = productService.create(
				new Product(TEST_PRODUCT_NAME, "Desc", new BigDecimal("10.00"), 10, 2, Category.TOYS));
		productService.updateStock(p.getId(), 5);
		Product updatedP = productService.read(p.getId()).orElseThrow();
		assertEquals(15, updatedP.getQuantity(), "Stock should increase to 15");
		productService.updateStock(p.getId(), -3);
		updatedP = productService.read(p.getId()).orElseThrow();
		assertEquals(12, updatedP.getQuantity(), "Stock should decrease to 12");
	}

	@Test
	void testUpdateStock_NotEnoughStock() {
		System.out.println("Running: testUpdateStock_NotEnoughStock");
		Product p = productService
				.create(new Product(TEST_PRODUCT_NAME, "Desc", new BigDecimal("10.00"), 5, 2, Category.TOYS));
		Exception exception = assertThrows(RuntimeException.class, () -> {
			productService.updateStock(p.getId(), -10);
		});
		assertTrue(exception.getMessage().contains("Not enough stock"), "Should throw exception for negative stock");
	}

	@Test
	void testIsNeedRestock() {
		System.out.println("Running: testIsNeedRestock");
		Product p = productService.create(
				new Product(TEST_PRODUCT_NAME, "Desc", new BigDecimal("10.00"), 5, 10, Category.GROCERIES));
		assertTrue(productService.isNeedRestock(p.getId()), "Should need restock when Qty < MinStock");
		productService.updateStock(p.getId(), 15);
		assertFalse(productService.isNeedRestock(p.getId()), "Should NOT need restock when Qty > MinStock");
	}
}
