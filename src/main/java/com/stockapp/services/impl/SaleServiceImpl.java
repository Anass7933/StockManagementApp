package com.stockapp.services.impl;

import com.stockapp.models.entities.Sale;
import com.stockapp.models.entities.SaleItem;
import com.stockapp.services.interfaces.SaleService;
import com.stockapp.services.interfaces.SaleItemService;
import com.stockapp.services.interfaces.ProductService;
import com.stockapp.utils.DatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.ZoneOffset;

public class SaleServiceImpl implements SaleService {
	@Override
	public Sale create(Sale sale) {
		String sql = """
				INSERT INTO sales (total_price)
				VALUES (?)
				RETURNING id, created_at;
				""";

		try (Connection c = DatabaseUtils.getConnection();
				PreparedStatement ps = c.prepareStatement(sql)) {
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

		try (Connection c = DatabaseUtils.getConnection();
				PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Sale sale = new Sale(
							rs.getLong("id"),
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

		try (Connection c = DatabaseUtils.getConnection();
				PreparedStatement ps = c.prepareStatement(sql)) {
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

		try (Connection c = DatabaseUtils.getConnection();
				PreparedStatement ps = c.prepareStatement(sql)) {
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
				Sale sale = new Sale(
						rs.getLong("id"),
						rs.getLong("total_price"),
						rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC));
				sales.add(sale);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to read all sales", e);
		}

		return sales;
	}

	public Sale createSaleWithItems(Sale sale, List<SaleItem> items) {
		Connection c = null;
		try {
			c = DatabaseUtils.getConnection();
			c.setAutoCommit(false);

			// Insert the sale using the SaleServiceImpl's create method
			sale = this.create(sale); // create(Sale)

			// Use the SaleItemService to create sale items
			SaleItemService saleItemService = new SaleItemServiceImpl();
			ProductService productService = new ProductServiceImpl();

			for (SaleItem item : items) {
				item.setSaleId(sale.getId());

				// Explicitly call create on the SaleItemService
				item = saleItemService.create(item);

				// Update stock using existing method
				productService.updateStock(item.getProductId(), -item.getQuantity());
			}

			c.commit();
			return sale;

		} catch (SQLException e) {
			if (c != null) {
				try {
					c.rollback();
				} catch (SQLException rollbackEx) {
					rollbackEx.printStackTrace();
				}
			}
			throw new RuntimeException("Failed to create sale with items", e);
		} finally {
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

	@Override
	public void updateSaleWithItems(Long saleId, Sale sale, List<SaleItem> items) {
		// Use service implementations (explicit, non-static calls)
		SaleItemService saleItemService = new SaleItemServiceImpl();
		ProductService productService = new ProductServiceImpl();

		try {
			// 1) Get existing sale items and restore their quantities
			List<SaleItem> oldItems = saleItemService.findBySaleId(saleId);
			if (oldItems != null) {
				for (SaleItem oldItem : oldItems) {
					// restore stock
					productService.updateStock(oldItem.getProductId(), oldItem.getQuantity());
				}
			}

			// 2) Update the sale itself (ensure sale has the correct id)
			sale.setId(saleId);
			this.update(sale);

			// 3) Update sale items (this method should delete old items and insert/update
			// the new ones)
			saleItemService.updateSaleItems(saleId, items);

			// 4) Deduct quantities for the newly provided items
			if (items != null) {
				for (SaleItem item : items) {
					productService.updateStock(item.getProductId(), -item.getQuantity());
				}
			}

		} catch (Exception e) {
			// translate to runtime as before; you can add logging here
			throw new RuntimeException("Failed to update sale with items for sale ID: " + saleId, e);
		}
	}

	@Override
	public Long getTotalRevenue(Long saleId) {
		String sql = "SELECT total_price FROM sales WHERE id = ?";

		try (Connection c = DatabaseUtils.getConnection();
				PreparedStatement ps = c.prepareStatement(sql)) {
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
