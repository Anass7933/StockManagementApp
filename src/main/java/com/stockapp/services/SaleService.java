package com.stockapp.services;

import com.stockapp.models.Sale;
import com.stockapp.models.SaleItem;
import com.stockapp.utils.DatabaseUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleService {

    /* ========== CREATE SALE ========== */
    public Sale createSale(List<SaleItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Sale must contain at least one item");
        }

        String insertSaleSql = """
            INSERT INTO sales (total_price)
            VALUES (?)
            RETURNING id, created_at
        """;

        String insertSaleItemSql = """
            INSERT INTO sale_items (sale_id, product_id, quantity, unit_price)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseUtils.getConnection()) {
            conn.setAutoCommit(false); // start transaction

            // Calculate total price
            BigDecimal totalPrice = BigDecimal.ZERO;
            for (SaleItem item : items) {
                totalPrice = totalPrice.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            long saleId;
            OffsetDateTime createdAt;

            // Insert into sales
            try (PreparedStatement ps = conn.prepareStatement(insertSaleSql)) {
                ps.setBigDecimal(1, totalPrice);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        saleId = rs.getLong("id");
                        createdAt = rs.getObject("created_at", OffsetDateTime.class);
                    } else {
                        conn.rollback();
                        throw new RuntimeException("Failed to create sale");
                    }
                }
            }

            // Insert sale items and update product stock
            for (SaleItem item : items) {
                boolean updated = ProductService.decreaseStockIfAvailable(item.getProductId(), item.getQuantity(), conn);
                if (!updated) {
                    conn.rollback();
                    throw new RuntimeException("Insufficient stock for product ID: " + item.getProductId());
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSaleItemSql)) {
                    ps.setLong(1, saleId);
                    ps.setLong(2, item.getProductId());
                    ps.setInt(3, item.getQuantity());
                    ps.setBigDecimal(4, item.getUnitPrice());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return new Sale(saleId, totalPrice, createdAt, items);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create sale", e);
        }
    }

    /* ========== GET SALE BY ID ========== */
    public Sale getSaleById(long saleId) {
        String saleSql = "SELECT id, total_price, created_at FROM sales WHERE id = ?";
        String itemsSql = "SELECT product_id, quantity, unit_price FROM sale_items WHERE sale_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement salePs = conn.prepareStatement(saleSql);
             PreparedStatement itemsPs = conn.prepareStatement(itemsSql)) {

            salePs.setLong(1, saleId);
            Sale sale;
            try (ResultSet rs = salePs.executeQuery()) {
                if (rs.next()) {
                    sale = new Sale(
                            rs.getLong("id"),
                            rs.getBigDecimal("total_price"),
                            rs.getObject("created_at", OffsetDateTime.class),
                            new ArrayList<>()
                    );
                } else {
                    return null;
                }
            }

            itemsPs.setLong(1, saleId);
            try (ResultSet rs = itemsPs.executeQuery()) {
                while (rs.next()) {
                    sale.getItems().add(new SaleItem(
                            saleId,
                            rs.getLong("product_id"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price")
                    ));
                }
            }

            return sale;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch sale", e);
        }
    }

    /* ========== GET ALL SALES ========== */
    public List<Sale> getAllSales() {
        String saleSql = "SELECT id, total_price, created_at FROM sales ORDER BY id ASC";
        String itemsSql = "SELECT sale_id, product_id, quantity, unit_price FROM sale_items WHERE sale_id = ?";

        List<Sale> sales = new ArrayList<>();

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement salePs = conn.prepareStatement(saleSql);
             ResultSet saleRs = salePs.executeQuery()) {

            while (saleRs.next()) {
                long saleId = saleRs.getLong("id");
                Sale sale = new Sale(
                        saleId,
                        saleRs.getBigDecimal("total_price"),
                        saleRs.getObject("created_at", OffsetDateTime.class),
                        new ArrayList<>()
                );

                try (PreparedStatement itemsPs = conn.prepareStatement(itemsSql)) {
                    itemsPs.setLong(1, saleId);
                    try (ResultSet itemsRs = itemsPs.executeQuery()) {
                        while (itemsRs.next()) {
                            sale.getItems().add(new SaleItem(
                                    saleId,
                                    itemsRs.getLong("product_id"),
                                    itemsRs.getInt("quantity"),
                                    itemsRs.getBigDecimal("unit_price")
                            ));
                        }
                    }
                }

                sales.add(sale);
            }

            return sales;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch sales", e);
        }
    }

    /* ========== GET TOTAL REVENUE ========== */
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_price), 0) AS revenue FROM sales";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("revenue");
            } else {
                return BigDecimal.ZERO;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch total revenue", e);
        }
    }
}
