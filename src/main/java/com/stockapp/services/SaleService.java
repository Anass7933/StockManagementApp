package com.stockapp.services;

import com.stockapp.models.Product;
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
    public Sale createSale(Sale inputSale) {
        if (inputSale == null || inputSale.getItems() == null || inputSale.getItems().isEmpty()) {
            throw new IllegalArgumentException("Sale must contain at least one item");
        }

        // ensure totals are computed from model
        inputSale.finalizeSale();
        BigDecimal totalPrice = BigDecimal.valueOf(inputSale.calculateTotal());

        String insertSaleSql = """
            INSERT INTO sales (total_price)
            VALUES (?)
            RETURNING id, created_at
        """;

        String insertSaleItemSql = """
            INSERT INTO sale_items (sale_id, product_id, quantity, unit_price)
            VALUES (?, ?, ?, ?)
            RETURNING id
        """;

        try (Connection conn = DatabaseUtils.getConnection()) {
            conn.setAutoCommit(false);

            long saleId;
            OffsetDateTime createdAt;

            // insert sale and get generated id
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

            // insert sale items and decrease stock
            List<SaleItem> persistedItems = new ArrayList<>();
            for (SaleItem item : inputSale.getItems()) {
                long productId = item.getProduct().getId();
                int qty = item.getQuantity();

                // decrease stock atomically (uses ProductService static helper)
                boolean ok = ProductService.decreaseStockIfAvailable(productId, qty, conn);
                if (!ok) {
                    conn.rollback();
                    throw new RuntimeException("Insufficient stock for product ID: " + productId);
                }

                // insert sale_item and get its id
                long saleItemId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSaleItemSql)) {
                    ps.setLong(1, saleId);
                    ps.setLong(2, productId);
                    ps.setInt(3, qty);
                    ps.setBigDecimal(4, BigDecimal.valueOf(item.getUnitPrice()));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            saleItemId = rs.getLong("id");
                        } else {
                            conn.rollback();
                            throw new RuntimeException("Failed to insert sale item for product ID: " + productId);
                        }
                    }
                }

                // fetch fresh Product snapshot for returned item (keeps returned object consistent)
                Product prodSnapshot = fetchProductById(productId, conn);

                // construct SaleItem for returned Sale (SaleItem constructor takes int id in your model)
                SaleItem persisted = new SaleItem((int) saleItemId, prodSnapshot, qty, item.getUnitPrice());
                persistedItems.add(persisted);
            }

            conn.commit();

            // create a Sale object to return (we create a fresh one so saleId matches DB)
            Sale result = new Sale((int) saleId, null); // user is not stored in DB in your design
            for (SaleItem si : persistedItems) result.addItem(si);
            result.finalizeSale(); // compute total in model

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create sale", e);
        }
    }

    /* ========== GET SALE BY ID ========== */
    public Sale getSaleById(long saleId) {
        String saleSql = "SELECT id, total_price, created_at FROM sales WHERE id = ?";
        String itemsSql = "SELECT id, product_id, quantity, unit_price FROM sale_items WHERE sale_id = ? ORDER BY id ASC";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement salePs = conn.prepareStatement(saleSql);
             PreparedStatement itemsPs = conn.prepareStatement(itemsSql)) {

            salePs.setLong(1, saleId);
            try (ResultSet rs = salePs.executeQuery()) {
                if (!rs.next()) return null;
            }

            // build Sale using available constructor (user not stored)
            Sale sale = new Sale((int) saleId, null);

            // fetch items
            itemsPs.setLong(1, saleId);
            try (ResultSet rs = itemsPs.executeQuery()) {
                while (rs.next()) {
                    int saleItemId = rs.getInt("id");
                    long productId = rs.getLong("product_id");
                    int quantity = rs.getInt("quantity");
                    double unitPrice = rs.getBigDecimal("unit_price").doubleValue();

                    Product product = fetchProductById(productId, conn);
                    SaleItem item = new SaleItem(saleItemId, product, quantity, unitPrice);
                    sale.addItem(item);
                }
            }

            sale.finalizeSale(); // compute totalAmount on model
            return sale;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch sale", e);
        }
    }

    /* ========== GET ALL SALES ========== */
    public List<Sale> getAllSales() {
        String saleSql = "SELECT id, total_price, created_at FROM sales ORDER BY id ASC";
        String itemsSql = "SELECT id, product_id, quantity, unit_price FROM sale_items WHERE sale_id = ? ORDER BY id ASC";

        List<Sale> sales = new ArrayList<>();

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement salePs = conn.prepareStatement(saleSql);
             ResultSet saleRs = salePs.executeQuery()) {

            while (saleRs.next()) {
                long saleId = saleRs.getLong("id");

                Sale sale = new Sale((int) saleId, null);

                try (PreparedStatement itemsPs = conn.prepareStatement(itemsSql)) {
                    itemsPs.setLong(1, saleId);
                    try (ResultSet itemsRs = itemsPs.executeQuery()) {
                        while (itemsRs.next()) {
                            int saleItemId = itemsRs.getInt("id");
                            long productId = itemsRs.getLong("product_id");
                            int quantity = itemsRs.getInt("quantity");
                            double unitPrice = itemsRs.getBigDecimal("unit_price").doubleValue();

                            Product product = fetchProductById(productId, conn);
                            SaleItem item = new SaleItem(saleItemId, product, quantity, unitPrice);
                            sale.addItem(item);
                        }
                    }
                }

                sale.finalizeSale();
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

    /* ======= Helper: fetch Product by id within same connection ======= */
    private Product fetchProductById(long productId, Connection conn) throws SQLException {
        String sql = """
            SELECT id, name, description, price, quantity, min_stock, created_at, category
            FROM products
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
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
                    throw new RuntimeException("Product not found (id=" + productId + ")");
                }
            }
        }
    }
}
