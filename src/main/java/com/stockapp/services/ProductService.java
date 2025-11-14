package com.stockapp.services;

import com.stockapp.models.Product;
import com.stockapp.models.User;
import com.stockapp.models.UserRole;
import com.stockapp.utils.DatabaseUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    /* ========== ADD PRODUCT ========== */
    public Product addProduct(Product product, User currentUser) {
		AuthService.requireRole(currentUser,UserRole.STOCK_MANAGER);


        String sql = """
            INSERT INTO products (name, description, price, quantity, min_stock)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id, created_at;
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getQuantity());
            ps.setInt(5, product.getMinStock());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
                    // return a fresh Product populated with DB-generated values
                    return new Product(
                            id,
                            product.getName(),
                            product.getDescription(),
                            product.getPrice(),
                            product.getQuantity(),
                            product.getMinStock(),
                            createdAt
                    );
                } else {
                    throw new RuntimeException("Failed to insert product");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add product", e);
        }
    }

    /* ========== UPDATE PRODUCT ========== */
    public Product updateProduct(Product product, User currentUser) {

		AuthService.requireRole(currentUser,UserRole.STOCK_MANAGER,UserRole.CASHIER);

        String sql = """
            UPDATE products
            SET name = ?, description = ?, price = ?, quantity = ?, min_stock = ?
            WHERE id = ?
            RETURNING created_at;
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getQuantity());
            ps.setInt(5, product.getMinStock());
            ps.setLong(6, product.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
                    // return updated Product instance reflecting DB state
                    return new Product(
                            product.getId(),
                            product.getName(),
                            product.getDescription(),
                            product.getPrice(),
                            product.getQuantity(),
                            product.getMinStock(),
                            createdAt
                    );
                } else {
                    throw new RuntimeException("Product not found or not updated (id=" + product.getId() + ")");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update product", e);
        }
    }

    /* ========== DELETE PRODUCT ========== */
    public void deleteProduct(long id, User currentUser) {
		AuthService.requireRole(currentUser,UserRole.STOCK_MANAGER);

        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                throw new RuntimeException("No product deleted (id=" + id + ")");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    /* ========== GET PRODUCT BY ID ========== */
    public Product getProductById(long id) {
        String sql = """
            SELECT id, name, description, price, quantity, min_stock, created_at
            FROM products
            WHERE id = ?
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getBigDecimal("price"),
                            rs.getInt("quantity"),
                            rs.getInt("min_stock"),
                            rs.getObject("created_at", OffsetDateTime.class)
                    );
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch product", e);
        }
    }

    /* ========== GET ALL PRODUCTS ========== */
    public List<Product> getAllProducts() {
        String sql = """
            SELECT id, name, description, price, quantity, min_stock, created_at
            FROM products
            ORDER BY id ASC
        """;

        List<Product> products = new ArrayList<>();

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity"),
                        rs.getInt("min_stock"),
                        rs.getObject("created_at", OffsetDateTime.class)
                ));
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch products", e);
        }
    }

    /* ========== UPDATE STOCK (package-private, used by SaleService/RestockService) ========== */
    /**
     * Atomically increase product stock. Returns true if updated.
     */
    boolean increaseStockAtomic(long productId, int amount, Connection conn) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setLong(2, productId);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    /**
     * Atomically decrease product stock only if enough stock exists.
     * Returns true if stock was decreased; false if insufficient stock.
     */
    boolean decreaseStockIfAvailable(long productId, int amount, Connection conn) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setLong(2, productId);
            ps.setInt(3, amount);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }
}
