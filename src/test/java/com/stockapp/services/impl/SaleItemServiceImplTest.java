package com.stockapp.services.impl;

import com.stockapp.models.entities.Sale;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.models.entities.Product;
import com.stockapp.models.enums.Category;
import com.stockapp.services.interfaces.SaleItemService;
import com.stockapp.services.interfaces.SaleService;
import com.stockapp.services.interfaces.ProductService;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Improved integration test for SaleItemServiceImpl
 * Tests sale item operations with better isolation and edge case coverage
 */
public class SaleItemServiceImplTest {

    private static final double DELTA = 0.01;

    private SaleItemService saleItemService;
    private SaleService saleService;
    private ProductService productService;

    private Sale testSale;
    private Product testProduct;
    private List<Long> createdSaleItemIds;

    @BeforeEach
    public void setUp() {
        saleItemService = new SaleItemServiceImpl();
        saleService = new SaleServiceImpl();
        productService = new ProductServiceImpl();
        createdSaleItemIds = new ArrayList<>();

        // Create a test product with sufficient stock
        testProduct = new Product(
                "Test SaleItem Product",
                "Product for sale item tests",
                new BigDecimal("15.00"),
                100,
                10,
                Category.BOOKS);
        testProduct = productService.create(testProduct);

        // Create a test sale
        testSale = new Sale(500L);
        testSale = saleService.create(testSale);
    }

    @AfterEach
    public void tearDown() {
        // Clean up in reverse order of creation to maintain referential integrity

        // Delete all sale items created in this test
        try {
            saleItemService.delete(testSale.getId());
        } catch (Exception e) {
            System.err.println("Warning: Could not delete sale items: " + e.getMessage());
        }

        // Clean up test sale
        if (testSale != null && testSale.getId() > 0) {
            try {
                saleService.delete(testSale.getId());
            } catch (Exception e) {
                System.err.println("Warning: Could not delete test sale: " + e.getMessage());
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
    @DisplayName("Should create sale item successfully with valid data")
    public void testCreate_ValidData_Success() {
        SaleItem saleItem = createTestSaleItem(5, 15.00);

        SaleItem created = saleItemService.create(saleItem);

        assertNotNull(created, "Created sale item should not be null");
        assertTrue(created.getId() > 0, "Created sale item should have valid ID");
        assertEquals(testSale.getId(), created.getSaleId());
        assertEquals(testProduct.getId(), created.getProductId());
        assertEquals(5, created.getQuantity());
        assertEquals(15.00, created.getUnitPrice(), DELTA);
        assertEquals(75.00, created.getLineTotal(), DELTA, "Line total should be quantity * unit price");
    }

    @Test
    @DisplayName("Should calculate line total correctly for various quantities and prices")
    public void testCreate_CalculatesLineTotalCorrectly() {
        SaleItem saleItem = createTestSaleItem(7, 12.50);

        SaleItem created = saleItemService.create(saleItem);

        assertEquals(87.50, created.getLineTotal(), DELTA, "Line total calculation incorrect");
    }

    @Test
    @DisplayName("Should handle decimal quantities and prices correctly")
    public void testCreate_DecimalValues_Success() {
        SaleItem saleItem = createTestSaleItem(3, 19.99);

        SaleItem created = saleItemService.create(saleItem);

        assertEquals(59.97, created.getLineTotal(), DELTA);
    }

    @Test
    @DisplayName("Should create multiple sale items in batch")
    public void testCreateSaleItems_MultiplItems_Success() {
        List<SaleItem> itemsToCreate = new ArrayList<>();
        itemsToCreate.add(createTestSaleItem(2, 15.00));
        itemsToCreate.add(createTestSaleItem(3, 15.00));

        List<SaleItem> created = saleItemService.creatSaleItems(itemsToCreate);

        assertNotNull(created, "Created items list should not be null");
        assertEquals(2, created.size(), "Should create 2 items");
        assertTrue(created.stream().allMatch(i -> i.getId() > 0),
                "All created items should have valid IDs");
        assertTrue(created.stream().allMatch(i -> i.getSaleId() == testSale.getId()),
                "All items should belong to the test sale");
    }

    // ==================== READ TESTS ====================

    @Test
    @DisplayName("Should read existing sale item by ID")
    public void testRead_ExistingId_ReturnsItem() {
        SaleItem created = saleItemService.create(createTestSaleItem(3, 15.00));

        Optional<SaleItem> found = saleItemService.read(created.getId());

        assertTrue(found.isPresent(), "Sale item should be found");
        assertEquals(created.getId(), found.get().getId());
        assertEquals(3, found.get().getQuantity());
        assertEquals(45.00, found.get().getLineTotal(), DELTA);
    }

    @Test
    @DisplayName("Should return empty Optional for non-existent ID")
    public void testRead_NonExistentId_ReturnsEmpty() {
        Optional<SaleItem> found = saleItemService.read(999999L);

        assertFalse(found.isPresent(), "Should return empty for non-existent ID");
    }

    @Test
    @DisplayName("Should return all sale items")
    public void testReadAll_ReturnsAllItems() {
        // Create test items
        SaleItem item1 = saleItemService.create(createTestSaleItem(2, 15.00));
        SaleItem item2 = saleItemService.create(createTestSaleItem(3, 15.00));

        List<SaleItem> allItems = saleItemService.readAll();

        assertNotNull(allItems, "Result should not be null");
        assertTrue(allItems.stream().anyMatch(i -> i.getId() == item1.getId()),
                "Should contain first created item");
        assertTrue(allItems.stream().anyMatch(i -> i.getId() == item2.getId()),
                "Should contain second created item");
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @DisplayName("Should update sale item successfully")
    public void testUpdate_ValidData_Success() {
        SaleItem created = saleItemService.create(createTestSaleItem(2, 15.00));

        created.setQuantity(5);
        created.setUnitPrice(20.00);
        SaleItem updated = saleItemService.update(created);

        assertNotNull(updated, "Updated item should not be null");
        assertEquals(5, updated.getQuantity(), "Quantity should be updated");
        assertEquals(20.00, updated.getUnitPrice(), DELTA, "Unit price should be updated");
        assertEquals(100.00, updated.getLineTotal(), DELTA, "Line total should be recalculated");
    }

    @Test
    @DisplayName("Should update multiple sale items for a sale")
    public void testUpdateSaleItems_ReplacesExistingItems() {
        // Create initial items
        saleItemService.create(createTestSaleItem(2, 15.00));

        // Create new items to replace old ones
        List<SaleItem> newItems = new ArrayList<>();
        newItems.add(createTestSaleItem(5, 20.00));
        newItems.add(createTestSaleItem(3, 20.00));

        saleItemService.updateSaleItems(testSale.getId(), newItems);

        List<SaleItem> updated = saleItemService.findBySaleId(testSale.getId());
        assertEquals(2, updated.size(), "Should have 2 items after update");
        assertTrue(updated.stream().allMatch(i -> i.getUnitPrice() == 20.00),
                "All items should have new unit price");
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Should delete all sale items for a specific sale")
    public void testDelete_DeletesAllItemsForSale() {
        // Create multiple items
        saleItemService.create(createTestSaleItem(2, 15.00));
        saleItemService.create(createTestSaleItem(3, 15.00));

        saleItemService.delete(testSale.getId());

        List<SaleItem> items = saleItemService.findBySaleId(testSale.getId());
        assertEquals(0, items.size(), "All sale items should be deleted");
    }

    // ==================== QUERY TESTS ====================

    @Test
    @DisplayName("Should find all items for a specific sale")
    public void testFindBySaleId_ReturnsItemsForSale() {

        List<SaleItem> items = saleItemService.findBySaleId(testSale.getId());

        assertNotNull(items, "Result should not be null");
        assertEquals(2, items.size(), "Should return 2 items");
        assertTrue(items.stream().allMatch(i -> i.getSaleId() == testSale.getId()),
                "All items should belong to the test sale");
    }

    @Test
    @DisplayName("Should return empty list for sale with no items")
    public void testFindBySaleId_NoItems_ReturnsEmptyList() {
        Sale emptySale = saleService.create(new Sale(100L));

        List<SaleItem> items = saleItemService.findBySaleId(emptySale.getId());

        assertNotNull(items, "Result should not be null");
        assertEquals(0, items.size(), "Should return empty list");

        // Cleanup
        saleService.delete(emptySale.getId());
    }

    @Test
    @DisplayName("Should find all items for a specific product")
    public void testFindByProductId_ReturnsItemsForProduct() {
        saleItemService.create(createTestSaleItem(5, 15.00));
        saleItemService.create(createTestSaleItem(3, 15.00));

        List<SaleItem> items = saleItemService.findByProductId(testProduct.getId());

        assertNotNull(items, "Result should not be null");
        assertTrue(items.size() >= 2, "Should return at least 2 items");
        assertTrue(items.stream().allMatch(i -> i.getProductId() == testProduct.getId()),
                "All items should be for the test product");
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle sale item with quantity of 1")
    public void testCreate_MinimumQuantity_Success() {
        SaleItem saleItem = createTestSaleItem(1, 15.00);

        SaleItem created = saleItemService.create(saleItem);

        assertEquals(1, created.getQuantity());
        assertEquals(15.00, created.getLineTotal(), DELTA);
    }

    @Test
    @DisplayName("Should handle sale item with large quantity")
    public void testCreate_LargeQuantity_Success() {
        SaleItem saleItem = createTestSaleItem(50, 15.00);

        SaleItem created = saleItemService.create(saleItem);

        assertEquals(50, created.getQuantity());
        assertEquals(750.00, created.getLineTotal(), DELTA);
    }

    @Test
    @DisplayName("Should handle sale item with very small unit price")
    public void testCreate_SmallPrice_Success() {
        SaleItem saleItem = createTestSaleItem(100, 0.01);

        SaleItem created = saleItemService.create(saleItem);

        assertEquals(0.01, created.getUnitPrice(), DELTA);
        assertEquals(1.00, created.getLineTotal(), DELTA);
    }

    @Test
    @DisplayName("Should handle empty list when creating multiple items")
    public void testCreateSaleItems_EmptyList_Success() {
        List<SaleItem> emptyList = new ArrayList<>();

        List<SaleItem> created = saleItemService.creatSaleItems(emptyList);

        assertNotNull(created, "Result should not be null");
        assertEquals(0, created.size(), "Should return empty list");
    }

    // ==================== HELPER METHODS ====================


    private SaleItem createTestSaleItem(int quantity, double unitPrice) {
        SaleItem saleItem = new SaleItem();
        saleItem.setSaleId(testSale.getId());
        saleItem.setProductId(testProduct.getId());
        saleItem.setQuantity(quantity);
        saleItem.setUnitPrice(unitPrice);
        return saleItem;
    }
}