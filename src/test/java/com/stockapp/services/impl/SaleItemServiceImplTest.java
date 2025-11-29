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
 * Integration test for SaleItemServiceImpl
 * Tests sale item operations without using Mockito
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SaleItemServiceImplTest {

    private SaleItemService saleItemService;
    private SaleService saleService;
    private ProductService productService;
    private SaleItem testSaleItem;
    private Sale testSale;
    private Product testProduct;

    @BeforeEach
    public void setUp() {
        saleItemService = new SaleItemServiceImpl();
        saleService = new SaleServiceImpl();
        productService = new ProductServiceImpl();

        // Create a test product
        testProduct = new Product(
                "Test SaleItem Product",
                "Product for sale item tests",
                new BigDecimal("15.00"),
                100,
                10,
                Category.FOOD);
        testProduct = productService.create(testProduct);

        // Create a test sale
        testSale = new Sale(500L);
        testSale = saleService.create(testSale);
    }

    @AfterEach
    public void tearDown() {
        // Clean up test sale item
        if (testSaleItem != null && testSaleItem.getId() > 0) {
            try {
                // Sale items are deleted when sale is deleted
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }

        // Clean up test sale
        if (testSale != null && testSale.getId() > 0) {
            try {
                saleService.delete(testSale.getId());
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }

        // Clean up test product
        if (testProduct != null && testProduct.getId() > 0) {
            try {
                productService.delete(testProduct.getId());
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    @Order(1)
    public void testCreate_ShouldCreateSaleItemSuccessfully() {
        testSaleItem = new SaleItem();
        testSaleItem.setSaleId(testSale.getId());
        testSaleItem.setProductId(testProduct.getId());
        testSaleItem.setQuantity(5);
        testSaleItem.setUnitPrice(15.00);

        SaleItem createdItem = saleItemService.create(testSaleItem);

        assertNotNull(createdItem);
        assertTrue(createdItem.getId() > 0, "Created sale item should have valid ID");
        assertEquals(testSale.getId(), createdItem.getSaleId());
        assertEquals(testProduct.getId(), createdItem.getProductId());
        assertEquals(5, createdItem.getQuantity());
        assertEquals(15.00, createdItem.getUnitPrice());
        assertEquals(75.00, createdItem.getLineTotal(), 0.01, "Line total should be calculated");
    }

    @Test
    @Order(2)
    public void testRead_ShouldReturnSaleItemById() {
        // Create a sale item first
        testSaleItem = new SaleItem();
        testSaleItem.setSaleId(testSale.getId());
        testSaleItem.setProductId(testProduct.getId());
        testSaleItem.setQuantity(3);
        testSaleItem.setUnitPrice(15.00);
        testSaleItem = saleItemService.create(testSaleItem);

        // Read the sale item
        Optional<SaleItem> foundItem = saleItemService.read(testSaleItem.getId());

        assertTrue(foundItem.isPresent(), "Sale item should be found");
        assertEquals(testSaleItem.getId(), foundItem.get().getId());
        assertEquals(3, foundItem.get().getQuantity());
        assertEquals(45.00, foundItem.get().getLineTotal(), 0.01);
    }

    @Test
    @Order(3)
    public void testRead_ShouldReturnEmptyForNonExistentId() {
        Optional<SaleItem> foundItem = saleItemService.read(999999L);

        assertFalse(foundItem.isPresent(), "Should return empty for non-existent ID");
    }

    @Test
    @Order(4)
    public void testUpdate_ShouldUpdateSaleItemSuccessfully() {
        // Create a sale item first
        testSaleItem = new SaleItem();
        testSaleItem.setSaleId(testSale.getId());
        testSaleItem.setProductId(testProduct.getId());
        testSaleItem.setQuantity(2);
        testSaleItem.setUnitPrice(15.00);
        testSaleItem = saleItemService.create(testSaleItem);

        // Update the sale item
        testSaleItem.setQuantity(5);
        testSaleItem.setUnitPrice(20.00);

        SaleItem updatedItem = saleItemService.update(testSaleItem);

        assertNotNull(updatedItem);
        assertEquals(5, updatedItem.getQuantity());
        assertEquals(20.00, updatedItem.getUnitPrice());
        assertEquals(100.00, updatedItem.getLineTotal(), 0.01, "Line total should be recalculated");
    }

    @Test
    @Order(5)
    public void testDelete_ShouldDeleteAllSaleItemsForSale() {
        // Create sale items
        SaleItem item1 = new SaleItem();
        item1.setSaleId(testSale.getId());
        item1.setProductId(testProduct.getId());
        item1.setQuantity(2);
        item1.setUnitPrice(15.00);
        saleItemService.create(item1);

        SaleItem item2 = new SaleItem();
        item2.setSaleId(testSale.getId());
        item2.setProductId(testProduct.getId());
        item2.setQuantity(3);
        item2.setUnitPrice(15.00);
        saleItemService.create(item2);

        // Delete all items for the sale
        saleItemService.delete(testSale.getId());

        // Verify deletion
        List<SaleItem> items = saleItemService.findBySaleId(testSale.getId());
        assertEquals(0, items.size(), "All sale items should be deleted");
    }

    @Test
    @Order(6)
    public void testReadAll_ShouldReturnAllSaleItems() {
        // Create test sale item
        testSaleItem = new SaleItem();
        testSaleItem.setSaleId(testSale.getId());
        testSaleItem.setProductId(testProduct.getId());
        testSaleItem.setQuantity(4);
        testSaleItem.setUnitPrice(15.00);
        testSaleItem = saleItemService.create(testSaleItem);

        List<SaleItem> allItems = saleItemService.readAll();

        assertNotNull(allItems);
        assertTrue(allItems.size() > 0, "Should return at least one sale item");
        assertTrue(allItems.stream().anyMatch(i -> i.getId() == testSaleItem.getId()),
                "Should contain the created test sale item");
    }

    @Test
    @Order(7)
    public void testFindBySaleId_ShouldReturnItemsForSpecificSale() {
        // Create sale items for the test sale
        SaleItem item1 = new SaleItem();
        item1.setSaleId(testSale.getId());
        item1.setProductId(testProduct.getId());
        item1.setQuantity(2);
        item1.setUnitPrice(15.00);
        saleItemService.create(item1);

        SaleItem item2 = new SaleItem();
        item2.setSaleId(testSale.getId());
        item2.setProductId(testProduct.getId());
        item2.setQuantity(3);
        item2.setUnitPrice(15.00);
        saleItemService.create(item2);

        List<SaleItem> items = saleItemService.findBySaleId(testSale.getId());

        assertNotNull(items);
        assertEquals(2, items.size(), "Should return 2 items for the sale");
        assertTrue(items.stream().allMatch(i -> i.getSaleId() == testSale.getId()),
                "All items should belong to the test sale");
    }

    @Test
    @Order(8)
    public void testFindByProductId_ShouldReturnItemsForSpecificProduct() {
        // Create sale items with the test product
        testSaleItem = new SaleItem();
        testSaleItem.setSaleId(testSale.getId());
        testSaleItem.setProductId(testProduct.getId());
        testSaleItem.setQuantity(5);
        testSaleItem.setUnitPrice(15.00);
        testSaleItem = saleItemService.create(testSaleItem);

        List<SaleItem> items = saleItemService.findByProductId(testProduct.getId());

        assertNotNull(items);
        assertTrue(items.size() > 0, "Should return at least one item");
        assertTrue(items.stream().allMatch(i -> i.getProductId() == testProduct.getId()),
                "All items should be for the test product");
    }

    @Test
    @Order(9)
    public void testCreateSaleItems_ShouldCreateMultipleItems() {
        List<SaleItem> itemsToCreate = new ArrayList<>();

        SaleItem item1 = new SaleItem();
        item1.setSaleId(testSale.getId());
        item1.setProductId(testProduct.getId());
        item1.setQuantity(2);
        item1.setUnitPrice(15.00);
        itemsToCreate.add(item1);

        SaleItem item2 = new SaleItem();
        item2.setSaleId(testSale.getId());
        item2.setProductId(testProduct.getId());
        item2.setQuantity(3);
        item2.setUnitPrice(15.00);
        itemsToCreate.add(item2);

        List<SaleItem> createdItems = saleItemService.creatSaleItems(itemsToCreate);

        assertNotNull(createdItems);
        assertEquals(2, createdItems.size(), "Should create 2 items");
        assertTrue(createdItems.stream().allMatch(i -> i.getId() > 0),
                "All created items should have valid IDs");
    }

    @Test
    @Order(10)
    public void testUpdateSaleItems_ShouldReplaceExistingItems() {
        // Create initial items
        SaleItem oldItem = new SaleItem();
        oldItem.setSaleId(testSale.getId());
        oldItem.setProductId(testProduct.getId());
        oldItem.setQuantity(2);
        oldItem.setUnitPrice(15.00);
        saleItemService.create(oldItem);

        // Create new items to replace
        List<SaleItem> newItems = new ArrayList<>();
        SaleItem newItem1 = new SaleItem();
        newItem1.setProductId(testProduct.getId());
        newItem1.setQuantity(5);
        newItem1.setUnitPrice(20.00);
        newItems.add(newItem1);

        SaleItem newItem2 = new SaleItem();
        newItem2.setProductId(testProduct.getId());
        newItem2.setQuantity(3);
        newItem2.setUnitPrice(20.00);
        newItems.add(newItem2);

        // Update sale items
        saleItemService.updateSaleItems(testSale.getId(), newItems);

        // Verify old items were replaced
        List<SaleItem> updatedItems = saleItemService.findBySaleId(testSale.getId());
        assertEquals(2, updatedItems.size(), "Should have 2 new items");
        assertTrue(updatedItems.stream().allMatch(i -> i.getUnitPrice() == 20.00),
                "All items should have new unit price");
    }

    @Test
    @Order(11)
    public void testCreate_ShouldCalculateLineTotalCorrectly() {
        testSaleItem = new SaleItem();
        testSaleItem.setSaleId(testSale.getId());
        testSaleItem.setProductId(testProduct.getId());
        testSaleItem.setQuantity(7);
        testSaleItem.setUnitPrice(12.50);

        SaleItem createdItem = saleItemService.create(testSaleItem);

        assertEquals(87.50, createdItem.getLineTotal(), 0.01,
                "Line total should be quantity * unit price");
    }

    @Test
    @Order(12)
    public void testFindBySaleId_ShouldReturnEmptyListForSaleWithNoItems() {
        // Create a new sale without items
        Sale emptySale = new Sale(100L);
        emptySale = saleService.create(emptySale);

        List<SaleItem> items = saleItemService.findBySaleId(emptySale.getId());

        assertNotNull(items);
        assertEquals(0, items.size(), "Should return empty list for sale with no items");

        // Cleanup
        saleService.delete(emptySale.getId());
    }
}
