package com.stockapp.services.impl;

import com.stockapp.models.entities.Sale;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.models.entities.Product;
import com.stockapp.models.enums.Category;
import com.stockapp.services.interfaces.SaleService;
import com.stockapp.services.interfaces.ProductService;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Improved integration test for SaleServiceImpl
 * Tests sale operations with better isolation and comprehensive coverage
 */
public class SaleServiceImplTest {

    private SaleService saleService;
    private ProductService productService;

    private Sale testSale;
    private Product testProduct;
    private List<Long> createdSaleIds;

    @BeforeEach
    public void setUp() {
        saleService = new SaleServiceImpl();
        productService = new ProductServiceImpl();
        createdSaleIds = new ArrayList<>();

        // Create a test product for sale items with sufficient stock
        testProduct = new Product(
                "Test Sale Product",
                "Product for sale tests",
                new BigDecimal("10.00"),
                100,
                10,
                Category.ELECTRONICS);
        testProduct = productService.create(testProduct);
    }

    @AfterEach
    public void tearDown() {
        // Clean up in reverse order to maintain referential integrity

        // Clean up test sale
        if (testSale != null && testSale.getId() > 0) {
            try {
                saleService.delete(testSale.getId());
            } catch (Exception e) {
                System.err.println("Warning: Could not delete test sale: " + e.getMessage());
            }
        }

        // Clean up any additional sales created during tests
        for (Long saleId : createdSaleIds) {
            try {
                saleService.delete(saleId);
            } catch (Exception e) {
                System.err.println("Warning: Could not delete sale " + saleId + ": " + e.getMessage());
            }
        }

        // Clean up test product
        if (testProduct != null && testProduct.getId() > 0) {
            try {
                productService.delete(testProduct.getId());
            } catch (Exception e) {
                System.err.println("Warning: Could not delete test product: " + e.getMessage());
            }
        }
    }

    // ==================== CREATE TESTS ====================

    @Test
    @DisplayName("Should create sale successfully with valid data")
    public void testCreate_ValidData_Success() {
        testSale = new Sale(1000L);

        Sale created = saleService.create(testSale);

        assertNotNull(created, "Created sale should not be null");
        assertTrue(created.getId() > 0, "Created sale should have valid ID");
        assertEquals(1000L, created.getTotalPrice(), "Total price should match");
        assertNotNull(created.getCreatedAt(), "Created sale should have timestamp");
    }

    @Test
    @DisplayName("Should create sale with zero total price")
    public void testCreate_ZeroTotalPrice_Success() {
        testSale = new Sale(0L);

        Sale created = saleService.create(testSale);

        assertNotNull(created, "Created sale should not be null");
        assertEquals(0L, created.getTotalPrice(), "Total price should be zero");
    }

    @Test
    @DisplayName("Should create sale with large total price")
    public void testCreate_LargeTotalPrice_Success() {
        testSale = new Sale(999999L);

        Sale created = saleService.create(testSale);

        assertNotNull(created, "Created sale should not be null");
        assertEquals(999999L, created.getTotalPrice(), "Total price should match");
    }

    @Test
    @DisplayName("Should create sale with items successfully")
    public void testCreateSaleWithItems_ValidData_Success() {
        testSale = new Sale(100L);
        List<SaleItem> items = createTestSaleItems(5, 10.00);

        Sale created = saleService.createSaleWithItems(testSale, items);

        assertNotNull(created, "Created sale should not be null");
        assertTrue(created.getId() > 0, "Sale should have valid ID");

        // Verify stock was updated
        Optional<Product> updatedProduct = productService.read(testProduct.getId());
        assertTrue(updatedProduct.isPresent(), "Product should still exist");
        assertEquals(95, updatedProduct.get().getQuantity(),
                "Product stock should be reduced by quantity sold");
    }

    @Test
    @DisplayName("Should create sale with multiple items")
    public void testCreateSaleWithItems_MultipleItems_Success() {
        testSale = new Sale(200L);

        List<SaleItem> items = new ArrayList<>();
        items.add(createSaleItem(3, 10.00));
        items.add(createSaleItem(2, 10.00));

        Sale created = saleService.createSaleWithItems(testSale, items);

        assertNotNull(created, "Created sale should not be null");

        // Verify total stock reduction (3 + 2 = 5)
        Optional<Product> updatedProduct = productService.read(testProduct.getId());
        assertTrue(updatedProduct.isPresent(), "Product should still exist");
        assertEquals(95, updatedProduct.get().getQuantity(),
                "Product stock should be reduced by total quantity sold");
    }

    @Test
    @DisplayName("Should create sale with empty items list")
    public void testCreateSaleWithItems_EmptyList_Success() {
        testSale = new Sale(0L);
        List<SaleItem> emptyItems = new ArrayList<>();

        Sale created = saleService.createSaleWithItems(testSale, emptyItems);

        assertNotNull(created, "Created sale should not be null");

        // Verify stock unchanged
        Optional<Product> unchangedProduct = productService.read(testProduct.getId());
        assertTrue(unchangedProduct.isPresent(), "Product should still exist");
        assertEquals(100, unchangedProduct.get().getQuantity(),
                "Product stock should remain unchanged");
    }

    // ==================== READ TESTS ====================

    @Test
    @DisplayName("Should read existing sale by ID")
    public void testRead_ExistingId_ReturnsSale() {
        testSale = saleService.create(new Sale(500L));

        Optional<Sale> found = saleService.read(testSale.getId());

        assertTrue(found.isPresent(), "Sale should be found");
        assertEquals(testSale.getId(), found.get().getId(), "IDs should match");
        assertEquals(500L, found.get().getTotalPrice(), "Total price should match");
    }

    @Test
    @DisplayName("Should return empty Optional for non-existent ID")
    public void testRead_NonExistentId_ReturnsEmpty() {
        Optional<Sale> found = saleService.read(999999L);

        assertFalse(found.isPresent(), "Should return empty for non-existent ID");
    }

    @Test
    @DisplayName("Should return all sales")
    public void testReadAll_ReturnsAllSales() {
        testSale = saleService.create(new Sale(2000L));

        List<Sale> allSales = saleService.readAll();

        assertNotNull(allSales, "Result should not be null");
        assertTrue(allSales.size() > 0, "Should return at least one sale");
        assertTrue(allSales.stream().anyMatch(s -> s.getId() == testSale.getId()),
                "Should contain the created test sale");
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @DisplayName("Should update sale successfully")
    public void testUpdate_ValidData_Success() {
        testSale = saleService.create(new Sale(1000L));

        Sale updated = new Sale(
                testSale.getId(),
                1500L,
                testSale.getCreatedAt());

        Sale result = saleService.update(updated);

        assertNotNull(result, "Updated sale should not be null");
        assertEquals(testSale.getId(), result.getId(), "ID should remain the same");
        assertEquals(1500L, result.getTotalPrice(), "Total price should be updated");
    }

    @Test
    @DisplayName("Should update sale from non-zero to zero price")
    public void testUpdate_ToZeroPrice_Success() {
        testSale = saleService.create(new Sale(1000L));

        Sale updated = new Sale(
                testSale.getId(),
                0L,
                testSale.getCreatedAt());

        Sale result = saleService.update(updated);

        assertNotNull(result, "Updated sale should not be null");
        assertEquals(0L, result.getTotalPrice(), "Total price should be updated to zero");
    }

    @Test
    @DisplayName("Should preserve timestamp when updating sale")
    public void testUpdate_PreservesTimestamp_Success() {
        testSale = saleService.create(new Sale(1000L));
        OffsetDateTime originalTimestamp = testSale.getCreatedAt();

        Sale updated = new Sale(
                testSale.getId(),
                1500L,
                originalTimestamp);

        Sale result = saleService.update(updated);

        assertEquals(originalTimestamp, result.getCreatedAt(),
                "Timestamp should remain unchanged");
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Should delete sale successfully")
    public void testDelete_ExistingSale_Success() {
        testSale = saleService.create(new Sale(750L));
        long saleId = testSale.getId();

        saleService.delete(saleId);

        Optional<Sale> deleted = saleService.read(saleId);
        assertFalse(deleted.isPresent(), "Sale should be deleted");

        testSale = null; // Prevent cleanup from trying to delete again
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent sale")
    public void testDelete_NonExistentSale_ThrowsException() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            saleService.delete(999999L);
        });

        assertNotNull(exception.getMessage(), "Exception should have a message");
        assertTrue(exception.getMessage().contains("No sale found") ||
                        exception.getMessage().contains("not found"),
                "Exception message should indicate sale not found");
    }

    // ==================== BUSINESS LOGIC TESTS ====================

    @Test
    @DisplayName("Should calculate total revenue correctly")
    public void testGetTotalRevenue_ValidSale_ReturnsCorrectRevenue() {
        testSale = saleService.create(new Sale(3500L));

        Long revenue = saleService.getTotalRevenue(testSale.getId());

        assertEquals(3500L, revenue, "Should return correct total revenue");
    }

    @Test
    @DisplayName("Should return zero revenue for sale with zero price")
    public void testGetTotalRevenue_ZeroPrice_ReturnsZero() {
        testSale = saleService.create(new Sale(0L));

        Long revenue = saleService.getTotalRevenue(testSale.getId());

        assertEquals(0L, revenue, "Should return zero revenue");
    }

    @Test
    @DisplayName("Should throw exception when getting revenue for non-existent sale")
    public void testGetTotalRevenue_NonExistentSale_ThrowsException() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            saleService.getTotalRevenue(999999L);
        });

        assertNotNull(exception.getMessage(), "Exception should have a message");
        assertTrue(exception.getMessage().contains("No sale found") ||
                        exception.getMessage().contains("not found"),
                "Exception message should indicate sale not found");
    }

    // ==================== TRANSACTION & ROLLBACK TESTS ====================

    @Test
    @DisplayName("Should rollback sale creation when insufficient stock")
    public void testCreateSaleWithItems_InsufficientStock_RollsBack() {
        testSale = new Sale(1000L);

        List<SaleItem> items = createTestSaleItems(200, 10.00); // More than available stock

        Exception exception = assertThrows(RuntimeException.class, () -> {
            saleService.createSaleWithItems(testSale, items);
        });

        assertNotNull(exception.getMessage(), "Exception should have a message");
        assertTrue(exception.getMessage().contains("Failed to create sale") ||
                        exception.getMessage().contains("insufficient") ||
                        exception.getMessage().contains("stock"),
                "Exception should indicate sale creation failure");

        // Verify stock was not changed
        Optional<Product> unchangedProduct = productService.read(testProduct.getId());
        assertTrue(unchangedProduct.isPresent(), "Product should still exist");
        assertEquals(100, unchangedProduct.get().getQuantity(),
                "Product stock should remain unchanged after rollback");

        testSale = null; // No sale was created, prevent cleanup attempt
    }

    @Test
    @DisplayName("Should rollback when sale items reference invalid product")
    public void testCreateSaleWithItems_InvalidProduct_RollsBack() {
        testSale = new Sale(500L);

        List<SaleItem> items = new ArrayList<>();
        SaleItem invalidItem = new SaleItem();
        invalidItem.setProductId(999999L); // Non-existent product
        invalidItem.setQuantity(5);
        invalidItem.setUnitPrice(10.00);
        items.add(invalidItem);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            saleService.createSaleWithItems(testSale, items);
        });

        assertNotNull(exception, "Should throw exception for invalid product");

        testSale = null; // No sale was created
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle sale with minimum valid price")
    public void testCreate_MinimumPrice_Success() {
        testSale = new Sale(1L);

        Sale created = saleService.create(testSale);

        assertNotNull(created, "Created sale should not be null");
        assertEquals(1L, created.getTotalPrice(), "Total price should be 1");
    }

    @Test
    @DisplayName("Should handle creating multiple sales in sequence")
    public void testCreate_MultipleSales_Success() {
        Sale sale1 = saleService.create(new Sale(100L));
        createdSaleIds.add(sale1.getId());

        Sale sale2 = saleService.create(new Sale(200L));
        createdSaleIds.add(sale2.getId());

        Sale sale3 = saleService.create(new Sale(300L));
        testSale = sale3; // Will be cleaned up by @AfterEach

        assertTrue(sale1.getId() > 0, "First sale should have valid ID");
        assertTrue(sale2.getId() > 0, "Second sale should have valid ID");
        assertTrue(sale3.getId() > 0, "Third sale should have valid ID");
        assertNotEquals(sale1.getId(), sale2.getId(), "Sales should have different IDs");
        assertNotEquals(sale2.getId(), sale3.getId(), "Sales should have different IDs");
    }

    @Test
    @DisplayName("Should handle sale item with exact available stock")
    public void testCreateSaleWithItems_ExactStock_Success() {
        testSale = new Sale(1000L);
        List<SaleItem> items = createTestSaleItems(100, 10.00); // Exactly all available stock

        Sale created = saleService.createSaleWithItems(testSale, items);

        assertNotNull(created, "Sale should be created successfully");

        // Verify stock is now zero
        Optional<Product> depletedProduct = productService.read(testProduct.getId());
        assertTrue(depletedProduct.isPresent(), "Product should still exist");
        assertEquals(0, depletedProduct.get().getQuantity(),
                "Product stock should be completely depleted");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to create a list of test sale items
     */
    private List<SaleItem> createTestSaleItems(int quantity, double unitPrice) {
        List<SaleItem> items = new ArrayList<>();
        items.add(createSaleItem(quantity, unitPrice));
        return items;
    }

    /**
     * Helper method to create a single test sale item
     */
    private SaleItem createSaleItem(int quantity, double unitPrice) {
        SaleItem item = new SaleItem();
        item.setProductId(testProduct.getId());
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        return item;
    }
}