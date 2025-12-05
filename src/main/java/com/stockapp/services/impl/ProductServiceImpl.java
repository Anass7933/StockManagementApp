package com.stockapp.services.impl;

import com.stockapp.models.entities.Product;
import com.stockapp.models.enums.Category;
import com.stockapp.services.interfaces.ProductService;
import com.stockapp.utils.*;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {
	public Product create(Product product) {
		String sql = """
				    INSERT INTO products (name, description, price, quantity, min_stock, category)
				    VALUES (?, ?, ?, ?, ?, ?::category)
				    RETURNING id, created_at;
				""";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, product.getName());
			ps.setString(2, product.getDescription());
			ps.setBigDecimal(3, product.getPrice());
			ps.setInt(4, product.getQuantity());
			ps.setInt(5, product.getMinStock());
			ps.setString(6, product.getCategory().name());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					long id = rs.getLong("id");
					OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
					return new Product(id,
							product.getName(),
							product.getDescription(),
							product.getPrice(),
							product.getQuantity(),
							product.getMinStock(),
							createdAt,
							product.getCategory());
				} else {
					throw new RuntimeException("Failed to insert product");
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to add product", e);
		}
	}

	public Optional<Product> read(Long id) {
		String sql_query = """
				    SELECT id, name, description, price, quantity, min_stock, created_at, category
				    FROM products
				    WHERE id = ?
				""";
		try (Connection c = DatabaseUtils.getConnection();) {
			PreparedStatement ps = c.prepareStatement(sql_query);
			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				Product product = new Product(rs.getLong("id"),
						rs.getString("name"),
						rs.getString("description"),
						rs.getBigDecimal("price"),
						rs.getInt("quantity"),
						rs.getInt("min_stock"),
						rs.getObject("created_at", OffsetDateTime.class),
						Category.valueOf(rs.getString("category")));
				return Optional.of(product);
			} else {
				return Optional.empty();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error reading product", e);
		}
	}

	public Product update(Product product) {
		String sql = """
				    UPDATE products
				    SET name = ?, description = ?, price = ?, quantity = ?, min_stock = ?, category = ?::category
				    WHERE id = ?
				    RETURNING id,created_at;
				""";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, product.getName());
			ps.setString(2, product.getDescription());
			ps.setBigDecimal(3, product.getPrice());
			ps.setInt(4, product.getQuantity());
			ps.setInt(5, product.getMinStock());
			ps.setString(6, product.getCategory().name());
			ps.setLong(7, product.getId());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					long id = rs.getLong("id");
					OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
					return new Product(id,
							product.getName(),
							product.getDescription(),
							product.getPrice(),
							product.getQuantity(),
							product.getMinStock(),
							createdAt,
							product.getCategory());
				} else {
					throw new RuntimeException("Failed to update product");
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to update product", e);
		}
	}

	public void delete(Long id) {
		String sql = "DELETE FROM products WHERE id = ?";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, id);
			int deleted = ps.executeUpdate();
			if (deleted == 0) {
				throw new RuntimeException("No product deleted (id=" + id + ")");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to delete product", e);
		}
	}

	public List<Product> readAll() {
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
				products.add(new Product(rs.getLong("id"),
						rs.getString("name"),
						rs.getString("description"),
						rs.getBigDecimal("price"),
						rs.getInt("quantity"),
						rs.getInt("min_stock"),
						rs.getObject("created_at", OffsetDateTime.class),
						Category.valueOf(rs.getString("category"))));
			}
			return products;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch products", e);
		}
	}

	public Optional<Product> findByName(String name) {
		String sql = """
				    SELECT id, name, description, price, quantity, min_stock, created_at, category
				    FROM products
				    WHERE name = ?
				""";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return Optional.of(new Product(rs.getLong("id"),
							rs.getString("name"),
							rs.getString("description"),
							rs.getBigDecimal("price"),
							rs.getInt("quantity"),
							rs.getInt("min_stock"),
							rs.getObject("created_at", OffsetDateTime.class),
							Category.valueOf(rs.getString("category"))));
				} else {
					return Optional.empty();
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch product", e);
		}
	}

	public List<Product> findByCategory(String category) {
		String sql_query = "SELECT id, name, description, price, quantity, min_stock, created_at, category FROM products";
		List<Product> products = new ArrayList<>();
		try (Connection c = DatabaseUtils.getConnection();) {
			PreparedStatement ps = c.prepareStatement(sql_query);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Product user = new Product(rs.getLong("id"),
						rs.getString("name"),
						rs.getString("description"),
						rs.getBigDecimal("price"),
						rs.getInt("quantity"),
						rs.getInt("min_stock"),
						rs.getObject("created_at", OffsetDateTime.class),
						Category.valueOf(rs.getString("category")));
				products.add(user);
			}
			return products;
		} catch (SQLException e) {
			throw new RuntimeException("Error reading all products", e);
		}
	}

	public boolean isNeedRestock(Long productId) {
		String sql = "SELECT quantity,min_stock from products where id = ?";
		int quantity, min_stock;
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, productId);
			ResultSet rs = ps.executeQuery();
			rs.next();
			quantity = rs.getInt("quantity");
			min_stock = rs.getInt("min_stock");
			return quantity <= min_stock;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to check product", e);
		}
	}

	public void updateStock(Long productId, int amount) {
		String selectSql = "SELECT quantity FROM products WHERE id = ?";
		String updateSql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
		try (Connection c = DatabaseUtils.getConnection()) {
			int currentQuantity;
			try (PreparedStatement ps = c.prepareStatement(selectSql)) {
				ps.setLong(1, productId);
				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next()) {
						throw new RuntimeException("Product not found (id=" + productId + ")");
					}
					currentQuantity = rs.getInt("quantity");
				}
			}
			if (amount < 0 && currentQuantity + amount < 0) {
				throw new RuntimeException("Not enough stock for product (id=" + productId + ")");
			}
			try (PreparedStatement ps = c.prepareStatement(updateSql)) {
				ps.setInt(1, amount);
				ps.setLong(2, productId);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to update product stock", e);
		}
	}

	public int totalProducts() {
		return count("SELECT COUNT(*) FROM products");
	}

	public int lowStock() {
		return count("SELECT COUNT(*) FROM products WHERE quantity <= min_stock");
	}

	public int inStock() {
		return count("SELECT COUNT(*) FROM products WHERE quantity > 0");
	}

	public int outOfStock() {
		return count("SELECT COUNT(*) FROM products WHERE quantity = 0");
	}

	public int count(String query) {
		int x = 0;
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
			ResultSet rs = ps.executeQuery();
			rs.next();
			x = rs.getInt(1);
		} catch (SQLException e) {
			throw new RuntimeException("Failed to check product", e);
		}
		return x;
	}
}
