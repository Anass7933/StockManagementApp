package com.stockapp.services.impl;

import com.stockapp.models.entities.Product;
import com.stockapp.models.enums.Category;
import com.stockapp.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceImplTest {

    private ProductServiceImpl productService;
    private final String TEST_PRODUCT_NAME = "JUnit Integration Test Product";

    @BeforeEach
    void setUp() throws SQLException {
        productService = new ProductServiceImpl();
        // Ensure clean state
        deleteTestProduct();
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Always clean up the database
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

        // 1. Create a Product Object
        Product newProduct = new Product(
                TEST_PRODUCT_NAME,
                "Description for test",
                new BigDecimal("99.99"),
                10, // Quantity
                2,  // Min Stock
                Category.ELECTRONICS
        );

        // 2. Save to DB
        Product createdProduct = productService.create(newProduct);

        // 3. Verify Creation
        assertNotNull(createdProduct.getId(), "Product ID should be generated");
        assertNotNull(createdProduct.getCreatedAt(), "Created At should be generated");
        assertEquals(TEST_PRODUCT_NAME, createdProduct.getName());
        assertEquals(Category.ELECTRONICS, createdProduct.getCategory());

        // 4. Read back from DB using ID
        Optional<Product> fetchedProduct = productService.read(createdProduct.getId());
        
        assertTrue(fetchedProduct.isPresent(), "Should be able to find product by ID");
        assertEquals(new BigDecimal("99.99"), fetchedProduct.get().getPrice());
    }

    @Test
    void testUpdateStock_Success() {
        System.out.println("Running: testUpdateStock_Success");

        // 1. Create initial product with 10 items
        Product p = productService.create(new Product(
                TEST_PRODUCT_NAME, "Desc", new BigDecimal("10.00"), 10, 2, Category.TOYS
        ));

        // 2. Add 5 items
        productService.updateStock(p.getId(), 5);
        Product updatedP = productService.read(p.getId()).orElseThrow();
        assertEquals(15, updatedP.getQuantity(), "Stock should increase to 15");

        // 3. Remove 3 items
        productService.updateStock(p.getId(), -3);
        updatedP = productService.read(p.getId()).orElseThrow();
        assertEquals(12, updatedP.getQuantity(), "Stock should decrease to 12");
    }

    @Test
    void testUpdateStock_NotEnoughStock() {
        System.out.println("Running: testUpdateStock_NotEnoughStock");

        // 1. Create product with 5 items
        Product p = productService.create(new Product(
                TEST_PRODUCT_NAME, "Desc", new BigDecimal("10.00"), 5, 2, Category.TOYS
        ));

        // 2. Try to remove 10 items (Should fail)
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.updateStock(p.getId(), -10);
        });

        // 3. Verify Error Message
        assertTrue(exception.getMessage().contains("Not enough stock"), 
            "Should throw exception for negative stock");
    }

    @Test
    void testIsNeedRestock() {
        System.out.println("Running: testIsNeedRestock");

        // 1. Create product (Qty: 5, MinStock: 10) -> Needs Restock
        Product p = productService.create(new Product(
                TEST_PRODUCT_NAME, "Desc", new BigDecimal("10.00"), 5, 10, Category.GROCERIES
        ));

        assertTrue(productService.isNeedRestock(p.getId()), "Should need restock when Qty < MinStock");

        // 2. Update stock to 20 -> No Restock needed
        productService.updateStock(p.getId(), 15); // 5 + 15 = 20
        assertFalse(productService.isNeedRestock(p.getId()), "Should NOT need restock when Qty > MinStock");
    }
}
