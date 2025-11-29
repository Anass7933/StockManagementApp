package com.stockapp.utils;

import com.stockapp.models.entities.Product;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.models.enums.Category;
import com.stockapp.services.impl.ProductServiceImpl;
import com.stockapp.services.interfaces.ProductService;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CartManager
 * Tests cart functionality without using Mockito
 * Note: These tests require a test product in the database
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CartManagerTest {

    private CartManager cartManager;
    private ProductService productService;
    private Product testProduct;

    @BeforeEach
    public void setUp() {
        // Get singleton instance and clear it
        cartManager = CartManager.getInstance();
        cartManager.clearCart();

        productService = new ProductServiceImpl();

        // Create a test product with sufficient stock
        testProduct = new Product(
                "Test Product for Cart",
                "Test Description",
                new BigDecimal("10.00"),
                100, // quantity
                10, // min stock
                Category.ELECTRONICS);
        testProduct = productService.create(testProduct);
    }

    @AfterEach
    public void tearDown() {
        // Clean up: clear cart and delete test product
        cartManager.clearCart();
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
    public void testGetInstance_ShouldReturnSingletonInstance() {
        CartManager instance1 = CartManager.getInstance();
        CartManager instance2 = CartManager.getInstance();

        assertSame(instance1, instance2, "Should return same singleton instance");
    }

    @Test
    @Order(2)
    public void testAddItem_ShouldAddProductToCart() {
        cartManager.addItem(testProduct, 5);

        assertEquals(1, cartManager.getTotalItemCount(), "Cart should have 1 item");
        assertTrue(cartManager.containsProduct(testProduct), "Cart should contain the product");
        assertEquals(5, cartManager.getProductQuantityInCart(testProduct),
                "Cart should have correct quantity");
    }

    @Test
    @Order(3)
    public void testAddItem_ShouldThrowExceptionForZeroQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartManager.addItem(testProduct, 0);
        });

        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    @Order(4)
    public void testAddItem_ShouldThrowExceptionForNegativeQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartManager.addItem(testProduct, -5);
        });

        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    @Order(5)
    public void testAddItem_ShouldThrowExceptionWhenExceedingStock() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartManager.addItem(testProduct, 150); // More than available stock (100)
        });

        assertEquals("Quantity exceeds available stock", exception.getMessage());
    }

    @Test
    @Order(6)
    public void testAddItem_ShouldUpdateQuantityForExistingProduct() {
        cartManager.addItem(testProduct, 5);
        cartManager.addItem(testProduct, 3);

        assertEquals(1, cartManager.getTotalItemCount(), "Should still have 1 unique item");
        assertEquals(8, cartManager.getProductQuantityInCart(testProduct),
                "Quantity should be updated to 8");
    }

    @Test
    @Order(7)
    public void testAddItem_ShouldThrowExceptionWhenTotalExceedsStock() {
        cartManager.addItem(testProduct, 60);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartManager.addItem(testProduct, 50); // Total would be 110, exceeding stock of 100
        });

        assertEquals("Total quantity exceeds available stock", exception.getMessage());
    }

    @Test
    @Order(8)
    public void testRemoveItem_ShouldRemoveProductFromCart() {
        cartManager.addItem(testProduct, 5);
        SaleItem item = cartManager.getCartItems().get(0);

        cartManager.removeItem(item);

        assertEquals(0, cartManager.getTotalItemCount(), "Cart should be empty");
        assertFalse(cartManager.containsProduct(testProduct), "Cart should not contain the product");
    }

    @Test
    @Order(9)
    public void testUpdateItemQuantity_ShouldUpdateQuantity() {
        cartManager.addItem(testProduct, 5);
        SaleItem item = cartManager.getCartItems().get(0);

        cartManager.updateItemQuantity(item, 10);

        assertEquals(10, item.getQuantity(), "Quantity should be updated to 10");
    }

    @Test
    @Order(10)
    public void testUpdateItemQuantity_ShouldRemoveItemWhenQuantityIsZero() {
        cartManager.addItem(testProduct, 5);
        SaleItem item = cartManager.getCartItems().get(0);

        cartManager.updateItemQuantity(item, 0);

        assertEquals(0, cartManager.getTotalItemCount(), "Item should be removed from cart");
    }

    @Test
    @Order(11)
    public void testIncrementQuantity_ShouldIncreaseQuantityByOne() {
        cartManager.addItem(testProduct, 5);
        SaleItem item = cartManager.getCartItems().get(0);

        cartManager.incrementQuantity(item);

        assertEquals(6, item.getQuantity(), "Quantity should be incremented to 6");
    }

    @Test
    @Order(12)
    public void testDecrementQuantity_ShouldDecreaseQuantityByOne() {
        cartManager.addItem(testProduct, 5);
        SaleItem item = cartManager.getCartItems().get(0);

        cartManager.decrementQuantity(item);

        assertEquals(4, item.getQuantity(), "Quantity should be decremented to 4");
    }

    @Test
    @Order(13)
    public void testDecrementQuantity_ShouldRemoveItemWhenQuantityBecomesZero() {
        cartManager.addItem(testProduct, 1);
        SaleItem item = cartManager.getCartItems().get(0);

        cartManager.decrementQuantity(item);

        assertEquals(0, cartManager.getTotalItemCount(), "Item should be removed when quantity becomes 0");
    }

    @Test
    @Order(14)
    public void testGetTotalPrice_ShouldCalculateCorrectTotal() {
        cartManager.addItem(testProduct, 5);

        double expectedTotal = 5 * 10.00; // 5 items at $10.00 each
        assertEquals(expectedTotal, cartManager.getTotalPrice(), 0.01,
                "Total price should be calculated correctly");
    }

    @Test
    @Order(15)
    public void testIsEmpty_ShouldReturnTrueForEmptyCart() {
        assertTrue(cartManager.isEmpty(), "Empty cart should return true");
    }

    @Test
    @Order(16)
    public void testIsEmpty_ShouldReturnFalseForNonEmptyCart() {
        cartManager.addItem(testProduct, 1);

        assertFalse(cartManager.isEmpty(), "Non-empty cart should return false");
    }

    @Test
    @Order(17)
    public void testClearCart_ShouldRemoveAllItems() {
        cartManager.addItem(testProduct, 5);

        cartManager.clearCart();

        assertEquals(0, cartManager.getTotalItemCount(), "Cart should be empty after clear");
        assertTrue(cartManager.isEmpty(), "Cart should be empty");
    }

    @Test
    @Order(18)
    public void testGetSaleItems_ShouldReturnUnmodifiableList() {
        cartManager.addItem(testProduct, 5);

        var saleItems = cartManager.getSaleItems();

        assertNotNull(saleItems);
        assertEquals(1, saleItems.size());

        // Verify it's unmodifiable by attempting to modify
        assertThrows(UnsupportedOperationException.class, () -> {
            saleItems.clear();
        });
    }

    @Test
    @Order(19)
    public void testContainsProduct_ShouldReturnFalseForProductNotInCart() {
        assertFalse(cartManager.containsProduct(testProduct),
                "Should return false for product not in cart");
    }

    @Test
    @Order(20)
    public void testGetProductQuantityInCart_ShouldReturnZeroForProductNotInCart() {
        assertEquals(0, cartManager.getProductQuantityInCart(testProduct),
                "Should return 0 for product not in cart");
    }
}
