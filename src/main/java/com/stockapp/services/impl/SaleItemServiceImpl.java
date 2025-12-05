package com.stockapp.services.impl;

import com.stockapp.models.entities.SaleItem;
import com.stockapp.services.interfaces.SaleItemService;
import com.stockapp.utils.DatabaseUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SaleItemServiceImpl implements SaleItemService {
	public SaleItem create(SaleItem saleItem) {
		String sql = """
				INSERT INTO sale_items (sale_id, product_id, quantity, unit_price)
				VALUES (?, ?, ?, ?)
				RETURNING id, line_total;
				""";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, saleItem.getSaleId());
			ps.setLong(2, saleItem.getProductId());
			ps.setInt(3, saleItem.getQuantity());
			ps.setDouble(4, saleItem.getUnitPrice());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					saleItem.setId(rs.getLong("id"));
					saleItem.setLineTotal(rs.getDouble("line_total"));
					return saleItem;
				} else {
					throw new RuntimeException("Failed to insert sale item");
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to add sale item", e);
		}
	}

	public void delete(Long Id) {
		String sql = "DELETE FROM sale_items WHERE id = ?";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, Id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to delete sale item  : " + Id, e);
		}
	}

	public Optional<SaleItem> read(Long id) {
		String sql = "SELECT id, sale_id, product_id, quantity, unit_price, line_total FROM sale_items WHERE id = ?";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					SaleItem saleItem = new SaleItem(rs.getLong("id"),
							rs.getLong("sale_id"),
							rs.getLong("product_id"),
							rs.getInt("quantity"),
							rs.getDouble("unit_price"),
							rs.getDouble("line_total"));
					return Optional.of(saleItem);
				} else {
					return Optional.empty();
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to read sale item with ID: " + id, e);
		}
	}

	public SaleItem update(SaleItem saleItem) {
		String sql = """
				UPDATE sale_items
				SET sale_id = ?, product_id = ?, quantity = ?, unit_price = ?
				WHERE id = ?
				RETURNING line_total;
				""";
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, saleItem.getSaleId());
			ps.setLong(2, saleItem.getProductId());
			ps.setInt(3, saleItem.getQuantity());
			ps.setDouble(4, saleItem.getUnitPrice());
			ps.setLong(5, saleItem.getId());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					saleItem.setLineTotal(rs.getDouble("line_total"));
					return saleItem;
				} else {
					throw new RuntimeException("Failed to update sale item with ID: " + saleItem.getId());
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to update sale item", e);
		}
	}

	public List<SaleItem> readAll() {
		String sql = "SELECT id, sale_id, product_id, quantity, unit_price, line_total FROM sale_items";
		List<SaleItem> saleItems = new ArrayList<>();
		try (Connection c = DatabaseUtils.getConnection();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				SaleItem saleItem = new SaleItem(rs.getLong("id"),
						rs.getLong("sale_id"),
						rs.getLong("product_id"),
						rs.getInt("quantity"),
						rs.getDouble("unit_price"),
						rs.getDouble("line_total"));
				saleItems.add(saleItem);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to read all sale items", e);
		}
		return saleItems;
	}

	public List<SaleItem> findByProductId(Long productId) {
		String sql = "SELECT id, sale_id, product_id, quantity, unit_price, line_total FROM sale_items WHERE product_id = ?";
		List<SaleItem> saleItems = new ArrayList<>();
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, productId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					SaleItem saleItem = new SaleItem(rs.getLong("id"),
							rs.getLong("sale_id"),
							rs.getLong("product_id"),
							rs.getInt("quantity"),
							rs.getDouble("unit_price"),
							rs.getDouble("line_total"));
					saleItems.add(saleItem);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to find sale items by product ID: " + productId, e);
		}
		return saleItems;
	}

	public List<SaleItem> findBySaleId(Long saleId) {
		String sql = "SELECT id, sale_id, product_id, quantity, unit_price, line_total FROM sale_items WHERE sale_id = ?";
		List<SaleItem> saleItems = new ArrayList<>();
		try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, saleId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					SaleItem saleItem = new SaleItem(rs.getLong("id"),
							rs.getLong("sale_id"),
							rs.getLong("product_id"),
							rs.getInt("quantity"),
							rs.getDouble("unit_price"),
							rs.getDouble("line_total"));
					saleItems.add(saleItem);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to find sale items by sale ID: " + saleId, e);
		}
		return saleItems;
	}
}
