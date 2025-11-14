package com.stockapp.services;

import com.stockapp.models.Product;
import com.stockapp.utils.DatabaseUtils;

import java.sql.*;

public class ProductService {

    /* ========== CREATE ========== */
    public Product createProduct(Product p) {

        String sql = """
            INSERT INTO products (name, description, price, quantity, min_stock)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id, created_at;
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getPrice());
            ps.setInt(4, p.getQuantity());
            ps.setInt(5, p.getMinStock());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.setId(rs.getLong("id"));
                }
            }

            return p;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create product", e);
        }
    }

    /* ========== UPDATE ========== */
    public Product updateProduct(Product p) {

        String sql = """
            UPDATE products
            SET name = ?,
                description = ?,
                price = ?,
                quantity = ?,
                min_stock = ?
            WHERE id = ?;
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getPrice());
            ps.setInt(4, p.getQuantity());
            ps.setInt(5, p.getMinStock());
            ps.setLong(6, p.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Product id=" + p.getId() + " not found");
            }

            return p;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update product", e);
        }
    }

    /* ========== DELETE ========== */
    public void deleteProduct(long id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Product id=" + id + " not found");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete product " + id, e);
        }
    }

    /* ========== FIND BY ID ========== */
    public Product findById(long id) {

        String sql = """
            SELECT id, name, description, price, quantity, min_stock, created_at
            FROM products
            WHERE id = ?;
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new Product(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity"),
                        rs.getInt("min_stock"),
                        rs.getObject("created_at", java.time.OffsetDateTime.class)
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch product id=" + id, e);
        }
    }
}
