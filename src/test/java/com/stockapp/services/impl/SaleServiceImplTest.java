package com.stockapp.services.impl;

import com.stockapp.models.entities.Sale;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.models.entities.Product;
import com.stockapp.models.enums.Category;
import com.stockapp.services.interfaces.SaleService;
import com.stockapp.services.interfaces.ProductService;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for SaleServiceImpl
 * Tests sale operations without using Mockito
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SaleServiceImplTest {

    private SaleService saleService;
    private ProductService productService;
    private Sale testSale;
    private Product testProduct;

    @BeforeEach
    public void setUp() {
        saleService = new SaleServiceImpl();
        productService = new ProductServiceImpl();

        // Create a test product for sale items
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
    public void testCreate_ShouldCreateSaleSuccessfully() {
        testSale = new Sale(1000L);

        Sale createdSale = saleService.create(testSale);

        assertNotNull(createdSale);
        assertTrue(createdSale.getId() > 0, "Created sale should have valid ID");
        assertEquals(1000L, createdSale.getTotalPrice());
        assertNotNull(createdSale.getCreatedAt(), "Created sale should have timestamp");
    }

    @Test
    @Order(2)
    public void testRead_ShouldReturnSaleById() {
        // Create a sale first
        testSale = new Sale(500L);
        testSale = saleService.create(testSale);

        // Read the sale
        Optional<Sale> foundSale = saleService.read(testSale.getId());

        assertTrue(foundSale.isPresent(), "Sale should be found");
        assertEquals(testSale.getId(), foundSale.get().getId());
        assertEquals(500L, foundSale.get().getTotalPrice());
    }

    @Test
    @Order(3)
    public void testRead_ShouldReturnEmptyForNonExistentId() {
        Optional<Sale> foundSale = saleService.read(999999L);

        assertFalse(foundSale.isPresent(), "Should return empty for non-existent ID");
    }

    @Test
    @Order(4)
    public void testUpdate_ShouldUpdateSaleSuccessfully() {
        // Create a sale first
        testSale = new Sale(1000L);
        testSale = saleService.create(testSale);

        // Update the sale
        Sale updatedSale = new Sale(
                testSale.getId(),
                1500L,
                testSale.getCreatedAt());

        Sale result = saleService.update(updatedSale);

        assertNotNull(result);
        assertEquals(1500L, result.getTotalPrice());
    }

    @Test
    @Order(5)
    public void testDelete_ShouldDeleteSaleSuccessfully() {
        // Create a sale first
        testSale = new Sale(750L);
        testSale = saleService.create(testSale);
        long saleId = testSale.getId();

        // Delete the sale
        saleService.delete(saleId);

        // Verify deletion
        Optional<Sale> deletedSale = saleService.read(saleId);
        assertFalse(deletedSale.isPresent(), "Sale should be deleted");

        testSale = null; // Prevent cleanup from trying to delete again
    }

    @Test
    @Order(6)
    public void testDelete_ShouldThrowExceptionForNonExistentSale() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            saleService.delete(999999L);
        });

        assertTrue(exception.getMessage().contains("No sale found"),
                "Should throw exception for non-existent sale");
    }

    @Test
    @Order(7)
    public void testReadAll_ShouldReturnAllSales() {
        // Create test sale
        testSale = new Sale(2000L);
        testSale = saleService.create(testSale);

        List<Sale> allSales = saleService.readAll();

        assertNotNull(allSales);
        assertTrue(allSales.size() > 0, "Should return at least one sale");
        assertTrue(allSales.stream().anyMatch(s -> s.getId() == testSale.getId()),
                "Should contain the created test sale");
    }

    @Test
    @Order(8)
    public void testCreateSaleWithItems_ShouldCreateSaleAndItems() {
        // Create sale with items
        testSale = new Sale(100L);

        List<SaleItem> items = new ArrayList<>();
        SaleItem item1 = new SaleItem();
        item1.setProductId(testProduct.getId());
        item1.setQuantity(5);
        item1.setUnitPrice(10.00);
        items.add(item1);

        Sale createdSale = saleService.createSaleWithItems(testSale, items);

        assertNotNull(createdSale);
        assertTrue(createdSale.getId() > 0);

        // Verify stock was updated
        Optional<Product> updatedProduct = productService.read(testProduct.getId());
        assertTrue(updatedProduct.isPresent());
        assertEquals(95, updatedProduct.get().getQuantity(),
                "Product stock should be reduced by 5");
    }

    @Test
    @Order(9)
    public void testGetTotalRevenue_ShouldReturnCorrectRevenue() {
        // Create a sale
        testSale = new Sale(3500L);
        testSale = saleService.create(testSale);

        Long revenue = saleService.getTotalRevenue(testSale.getId());

        assertEquals(3500L, revenue, "Should return correct total revenue");
    }

    @Test
    @Order(10)
    public void testGetTotalRevenue_ShouldThrowExceptionForNonExistentSale() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            saleService.getTotalRevenue(999999L);
        });

        assertTrue(exception.getMessage().contains("No sale found"),
                "Should throw exception for non-existent sale");
    }

    @Test
    @Order(11)
    public void testCreateSaleWithItems_ShouldRollbackOnError() {
        // Create sale with invalid item (insufficient stock)
        testSale = new Sale(1000L);

        List<SaleItem> items = new ArrayList<>();
        SaleItem item1 = new SaleItem();
        item1.setProductId(testProduct.getId());
        item1.setQuantity(200); // More than available stock
        item1.setUnitPrice(10.00);
        items.add(item1);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            saleService.createSaleWithItems(testSale, items);
        });

        assertTrue(exception.getMessage().contains("Failed to create sale with items"));

        // Verify stock was not changed
        Optional<Product> unchangedProduct = productService.read(testProduct.getId());
        assertTrue(unchangedProduct.isPresent());
        assertEquals(100, unchangedProduct.get().getQuantity(),
                "Product stock should remain unchanged after rollback");

        testSale = null; // No sale was created
    }

    @Test
    @Order(12)
    public void testCreate_ShouldHandleZeroTotalPrice() {
        testSale = new Sale(0L);

        Sale createdSale = saleService.create(testSale);

        assertNotNull(createdSale);
        assertEquals(0L, createdSale.getTotalPrice());
    }
}
