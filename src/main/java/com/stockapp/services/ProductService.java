package com.stockapp.services;

import com.stockapp.models.Product;
import com.stockapp.utils.DatabaseUtils;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductService {
    /* ========== CREATE PRODUCT ========== */
    public static Product createProduct(Product product) {
        String sql = """
            INSERT INTO products (name, description, price, quantity, min_stock, category)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id, created_at;
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getQuantity());
            ps.setInt(5, product.getMinStock());
			ps.setString(6,product.getCategory());

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
                            createdAt,
							product.getCategory()
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
    public static Product updateProduct(Product product) {
        String sql = """
            UPDATE products
            SET name = ?, description = ?, price = ?, quantity = ?, min_stock = ?, category = ?
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
			ps.setString(6, product.getCategory());
            ps.setLong(7, product.getId());

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
                            createdAt,
							product.getCategory()
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
    public static void deleteProduct(long id) {

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

    /* ========== GET PRODUCT BY Name FOR RECHERCHE ========== */
    public Product getProductByName(String name) {
        String sql = """
            SELECT id, name, description, price, quantity, min_stock, created_at, category
            FROM products
            WHERE name = ?
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getBigDecimal("price"),
                            rs.getInt("quantity"),
                            rs.getInt("min_stock"),
                            rs.getObject("created_at", OffsetDateTime.class),
							rs.getString("category")
                    );
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch product", e);
        }
    }


    /* ========== LOAD PRODUCTS ========== */
    public static List<Product> loadProducts() {
        String sql = """
            SELECT id, name, description, price, quantity, min_stock, created_at, category
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
                        rs.getObject("created_at", OffsetDateTime.class),
						rs.getString("category")
                ));
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch products", e);
        }
    }

    /* ========== UPDATE STOCK (package-private, used by SaleService/RestockService) ========== */
    public static void increaseStock(long productId, int amount) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";

        Connection c = DatabaseUtils.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setLong(2, productId);
            int updated = ps.executeUpdate();
        }
    }

    public static boolean decreaseStockIfAvailable(long productId, int amount, Connection conn) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setLong(2, productId);
            ps.setInt(3, amount);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }
	
	public static boolean isRestockNeeded(long productID, Connection conn) {
		String sql = "SELECT quantity,min_stock from products where id = ?";

		int quantity,min_stock;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productID);
            ResultSet rs = ps.executeQuery();
            rs.next();
			quantity = rs.getInt("quantity");
			min_stock = rs.getInt("min_stock");

			return quantity <= min_stock;
		}catch (SQLException e) {
            throw new RuntimeException("Failed to fetch products", e);
        }
	
	}

    /* ========== LOAD PRODUCT BY ID FOR MODIFICATION ========== */

    public static Optional<Product> loadProductById(long id) {
        String sql = " SELECT id, name, description, price, quantity, min_stock, category FROM products WHERE id = ? ";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Product(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getBigDecimal("price"),
                            rs.getInt("quantity"),
                            rs.getInt("min_stock"),
                            rs.getString("category")
                    ));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user by id: " + id, e);
        }
    }
}
