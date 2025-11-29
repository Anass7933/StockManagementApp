package com.stockapp.services.impl;

import com.stockapp.models.entities.Product;
import com.stockapp.models.enums.Category;
import com.stockapp.services.interfaces.ProductService;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductServiceImplTest {

    private ProductService productService;
    private Long cleanupId = null;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl();
    }

    @AfterEach
    void tearDown() {
        if (cleanupId != null && cleanupId > 0) {
            try { productService.delete(cleanupId); }
            catch (Exception ignored) {}
        }
        cleanupId = null;
    }

    private Product buildProduct(
            String name, String price,
            int qty, int minStock, Category category
    ) {
        return new Product(
                name,
                "desc",
                new BigDecimal(price),
                qty,
                minStock,
                category
        );
    }

    @Test @Order(1)
    void testCreate() {
        Product p = buildProduct("Create Test", "9.99", 50, 10, Category.ELECTRONICS);
        Product created = productService.create(p);
        cleanupId = created.getId();

        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertNotNull(created.getCreatedAt());
        assertEquals("Create Test", created.getName());
    }


    @Test @Order(2)
    void testRead_ExistingId() {
        Product p = productService.create(buildProduct("Read Test", "10.00", 20, 5, Category.FASHION));
        cleanupId = p.getId();

        Optional<Product> found = productService.read(p.getId());

        assertTrue(found.isPresent());
        assertEquals("Read Test", found.get().getName());
    }

    @Test @Order(3)
    void testRead_NonExisting() {
        Optional<Product> found = productService.read(999999L);
        assertFalse(found.isPresent());
    }
}
