package com.stockapp.services.impl;

import com.stockapp.models.entities.Sale;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.services.interfaces.SaleService;
import com.stockapp.utils.DatabaseUtils;
import java.sql.*;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SaleServiceImpl implements SaleService {
	@Override
	public Sale create(Sale sale) {
		String sql = """
				INSERT INTO sales (total_price)
				VALUES (?)
				RETURNING id, created_at;
				""";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setDouble(1, sale.getTotalPrice());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					sale.setId(rs.getLong("id"));
					sale.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC));
					return sale;
				} else {
					throw new RuntimeException("Failed to insert sale");
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to create sale", e);
		}
	}

	@Override
	public Optional<Sale> read(Long id) {
		String sql = "SELECT id, total_price, created_at FROM sales WHERE id = ?";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Sale sale = new Sale(rs.getLong("id"),
							rs.getLong("total_price"),
							rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC));
					return Optional.of(sale);
				} else {
					return Optional.empty();
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to read sale with ID: " + id, e);
		}
	}

	@Override
	public Sale update(Sale sale) {
		String sql = """
				UPDATE sales
				SET total_price = ?
				WHERE id = ?
				RETURNING created_at;
				""";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setDouble(1, sale.getTotalPrice());
			ps.setLong(2, sale.getId());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					sale.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC));
					return sale;
				} else {
					throw new RuntimeException("Failed to update sale with ID: " + sale.getId());
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to update sale", e);
		}
	}

	@Override
	public void delete(Long id) {
		String sql = "DELETE FROM sales WHERE id = ?";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, id);
			int rowsAffected = ps.executeUpdate();
			if (rowsAffected == 0) {
				throw new RuntimeException("No sale found with ID: " + id);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to delete sale with ID: " + id, e);
		}
	}

	@Override
	public List<Sale> readAll() {
		String sql = "SELECT id, total_price, created_at FROM sales ORDER BY created_at DESC";
		List<Sale> sales = new ArrayList<>();
		try (Connection c = DatabaseUtils.getConnection();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Sale sale = new Sale(rs.getLong("id"),
						rs.getLong("total_price"),
						rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC));
				sales.add(sale);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to read all sales", e);
		}
		return sales;
	}

	@Override
	public Sale createSaleWithItems(Sale sale, List<SaleItem> items) {
		Connection c = null;
		PreparedStatement psSale = null;
		PreparedStatement psItem = null;
		PreparedStatement psStock = null;
		try {
			c = DatabaseUtils.getConnection();
			c.setAutoCommit(false);
			String sqlSale = "INSERT INTO sales (total_price) VALUES (?) RETURNING id, created_at";
			psSale = c.prepareStatement(sqlSale);
			psSale.setDouble(1, sale.getTotalPrice());
			ResultSet rsSale = psSale.executeQuery();
			if (rsSale.next()) {
				sale.setId(rsSale.getLong("id"));
				sale.setCreatedAt(rsSale.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC));
			} else {
				throw new RuntimeException("Failed to insert sale header");
			}
			String sqlItem = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?) "
					+ "RETURNING id";
			psItem = c.prepareStatement(sqlItem);
			String sqlStock = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
			psStock = c.prepareStatement(sqlStock);
			for (SaleItem item : items) {
				psItem.setLong(1, sale.getId());
				psItem.setLong(2, item.getProductId());
				psItem.setInt(3, item.getQuantity());
				psItem.setDouble(4, item.getUnitPrice());
				ResultSet rsItem = psItem.executeQuery();
				if (rsItem.next()) {
					item.setId(rsItem.getLong("id"));
				}
				psStock.setInt(1, -item.getQuantity());
				psStock.setLong(2, item.getProductId());
				int rowsUpdated = psStock.executeUpdate();
				if (rowsUpdated == 0) {
					throw new SQLException("Product not found or failed to update stock for Product ID: " +
							item.getProductId());
				}
			}
			c.commit();
			return sale;
		} catch (Exception e) {
			if (c != null) {
				try {
					System.out.println("Transaction failed. Rolling back...");
					c.rollback();
				} catch (SQLException rollbackEx) {
					rollbackEx.printStackTrace();
				}
			}
			throw new RuntimeException("Failed to create sale with items", e);
		} finally {
			closeQuietly(psSale);
			closeQuietly(psItem);
			closeQuietly(psStock);
			if (c != null) {
				try {
					c.setAutoCommit(true);
					c.close();
				} catch (SQLException closeEx) {
					closeEx.printStackTrace();
				}
			}
		}
	}

	private void closeQuietly(AutoCloseable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (Exception ignored) {
			}
		}
	}

	@Override
	public Long getTotalRevenue(Long saleId) {
		String sql = "SELECT total_price FROM sales WHERE id = ?";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, saleId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getLong("total_price");
				} else {
					throw new RuntimeException("No sale found with ID: " + saleId);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to get total revenue for sale ID: " + saleId, e);
		}
	}
}
