package com.stockapp.services;

import com.stockapp.models.Product;
import com.stockapp.models.SaleItem;
import com.stockapp.models.User;
import com.stockapp.models.UserRole;
import com.stockapp.utils.DatabaseUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SaleService
 *
 * Responsibilities:
 *  - createSales (cashiers only)
 *  - fetch single sale (cashier->own sales only, supervisors and stock managers can view all)
 *  - list all sales (supervisor)
 *  - sales by date range (supervisor)
 *  - total revenue (supervisor)
 *
 * Notes:
 *  - Uses transactions to atomically create sale rows, sale_items rows and decrement products.stock.
 *  - Uses optimistic DB-side stock-check (UPDATE ... WHERE stock >= ?) to avoid race conditions.
 */
public class SaleService {

    private final ProductService productService;

    public SaleService(ProductService productService) {
        this.productService = productService;
    }


    /* ---------- Create a sale (cashier only) ----------
       Input: authenticated cashier user, list of SaleItem objects (each must contain product id & quantity)
       Returns: generated sale id
    */
    public long createSale(User user, List<SaleItem> items) {
        AuthService.requireRole(user, UserRole.CASHIER);

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Sale must contain at least one item");
        }

        // Calculate total and validate products first (read prices)
        BigDecimal total = BigDecimal.ZERO;
        List<Product> productsForItems = new ArrayList<>(items.size());

        for (SaleItem si : items) {
            // Expect SaleItem to have: getProduct().getId() and getQuantity()
            long productId = si.getProduct().getId();
			int qty = si.getQuantity();
            if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive for product " + productId);

            Product product = productService.getProductById(productId);
            if (product == null) throw new RuntimeException("Product with id " + productId + " not found");

            BigDecimal unitPrice = product.getPrice(); // expect BigDecimal or convert if different
            if (unitPrice == null) throw new RuntimeException("Product price is null for product " + productId);

            total = total.add(unitPrice.multiply(BigDecimal.valueOf(qty)));
            productsForItems.add(product);
        }

        String insertSaleSql = "INSERT INTO sales (total_amount, cashier_id) VALUES (?, ?) RETURNING id, created_at";
        String insertSaleItemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        String updateStockSql = "UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement psInsertSale = conn.prepareStatement(insertSaleSql);
             PreparedStatement psInsertItem = conn.prepareStatement(insertSaleItemSql);
             PreparedStatement psUpdateStock = conn.prepareStatement(updateStockSql)) {

            conn.setAutoCommit(false);

            // 1) insert sale
            psInsertSale.setBigDecimal(1, total);
            psInsertSale.setLong(2, user.getUserId());
            try (ResultSet rs = psInsertSale.executeQuery()) {
                if (!rs.next()) {
                    conn.rollback();
                    throw new RuntimeException("Failed to create sale record");
                }
            }

            // Retrieve the generated sale id (must re-query because above used executeQuery that returned result)
            long saleId;
            try (ResultSet rs = psInsertSale.getResultSet()) {
                if (rs != null && rs.next()) {
                    saleId = rs.getLong("id");
                } else {
                    // Fallback: select last inserted id for this cashier and total (less robust but kept for safety)
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT id FROM sales WHERE cashier_id = ? AND total_amount = ? ORDER BY created_at DESC LIMIT 1")) {
                        ps.setLong(1, user.getUserId());
                        ps.setBigDecimal(2, total);
                        try (ResultSet rs2 = ps.executeQuery()) {
                            if (rs2.next()) {
                                saleId = rs2.getLong("id");
                            } else {
                                conn.rollback();
                                throw new RuntimeException("Unable to determine created sale id");
                            }
                        }
                    }
                }
            }

            // 2) insert sale items & decrement stock for each item
            for (int i = 0; i < items.size(); i++) {
                SaleItem si = items.get(i);
                Product product = productsForItems.get(i);
                long productId = product.getId();
                int qty = si.getQuantity();
                BigDecimal unitPrice = product.getPrice();

                // Insert sale item
                psInsertItem.setLong(1, saleId);
                psInsertItem.setLong(2, productId);
                psInsertItem.setInt(3, qty);
                psInsertItem.setBigDecimal(4, unitPrice);
                int inserted = psInsertItem.executeUpdate();
                if (inserted == 0) {
                    conn.rollback();
                    throw new RuntimeException("Failed to insert sale item for product " + productId);
                }

                // Update stock atomically (only succeed if enough stock)
                psUpdateStock.setInt(1, qty);
                psUpdateStock.setLong(2, productId);
                psUpdateStock.setInt(3, qty);
                int rows = psUpdateStock.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    throw new RuntimeException("Insufficient stock for product id " + productId + ". Sale aborted.");
                }
            }

            conn.commit();
            return saleId;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while creating sale", e);
        }
    }

    /* ---------- Get sale by id ----------
       - Supervisors can fetch any sale
       - Stock managers can fetch any sale
       - Cashiers can fetch only their own sales
       Returns: a SaleRecord DTO (inner class) with sale data and items
    */
    public SaleRecord getSaleById(User user, long saleId) {
        AuthService.requireRole(user, UserRole.CASHIER, UserRole.SUPERVISOR, UserRole.STOCK_MANAGER);

        // If cashier, ensure they only fetch their own sale
        boolean restrictToCashier = user.getRole() == UserRole.CASHIER;

        String saleSql = "SELECT id, total_amount, cashier_id, created_at FROM sales WHERE id = ?";
        String itemsSql = "SELECT product_id, quantity, unit_price FROM sale_items WHERE sale_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement psSale = conn.prepareStatement(saleSql);
             PreparedStatement psItems = conn.prepareStatement(itemsSql)) {

            psSale.setLong(1, saleId);
            try (ResultSet rs = psSale.executeQuery()) {
                if (!rs.next()) throw new RuntimeException("Sale not found: id=" + saleId);

                long cashierId = rs.getLong("cashier_id");
                if (restrictToCashier && cashierId != user.getUserId()) {
                    throw new SecurityException("Cashier may only view their own sales");
                }

                BigDecimal total = rs.getBigDecimal("total_amount");
                OffsetDateTime created = rs.getObject("created_at", OffsetDateTime.class);

                // load items
                psItems.setLong(1, saleId);
                List<SaleItemRecord> itemList = new ArrayList<>();
                try (ResultSet irs = psItems.executeQuery()) {
                    while (irs.next()) {
                        long productId = irs.getLong("product_id");
                        int qty = irs.getInt("quantity");
                        BigDecimal unitPrice = irs.getBigDecimal("unit_price");

                        // attempt to fetch product for richer info; if missing, still include id+qty+price
                        Product product = null;
                        try {
                            product = productService.getProductById(productId);
                        } catch (Exception ignored) {}

                        itemList.add(new SaleItemRecord(productId, product, qty, unitPrice));
                    }
                }

                return new SaleRecord(saleId, total, cashierId, created, itemList);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching sale", e);
        }
    }

    /* ---------- Get all sales (supervisor only) ---------- */
    public List<SaleRecord> getAllSales(User user) {
        AuthService.requireRole(user, UserRole.SUPERVISOR);

        String sql = "SELECT id FROM sales ORDER BY created_at DESC";
        List<SaleRecord> out = new ArrayList<>();

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long saleId = rs.getLong("id");
                out.add(getSaleById(user, saleId));
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while listing sales", e);
        }
    }

    /* ---------- Get sales by date range (supervisor only) ---------- */
    public List<SaleRecord> getSalesByDateRange(User user, OffsetDateTime fromInclusive, OffsetDateTime toInclusive) {
        AuthService.requireRole(user, UserRole.SUPERVISOR);

        String sql = "SELECT id FROM sales WHERE created_at >= ? AND created_at <= ? ORDER BY created_at DESC";
        List<SaleRecord> out = new ArrayList<>();

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, fromInclusive);
            ps.setObject(2, toInclusive);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long saleId = rs.getLong("id");
                    out.add(getSaleById(user, saleId));
                }
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching sales by date", e);
        }
    }

    /* ---------- Get total revenue (supervisor) ---------- */
    public BigDecimal getTotalRevenue(User user) {
        AuthService.requireRole(user, UserRole.SUPERVISOR);

        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM sales";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("total");
            } else {
                return BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while calculating total revenue", e);
        }
    }

    /* ---------- DTOs returned by this service ---------- */
    public static class SaleRecord {
        public final long id;
        public final BigDecimal totalAmount;
        public final long cashierId;
        public final OffsetDateTime createdAt;
        public final List<SaleItemRecord> items;

        public SaleRecord(long id, BigDecimal totalAmount, long cashierId, OffsetDateTime createdAt, List<SaleItemRecord> items) {
            this.id = id;
            this.totalAmount = totalAmount;
            this.cashierId = cashierId;
            this.createdAt = createdAt;
            this.items = items;
        }
    }

    public static class SaleItemRecord {
        public final long productId;
        public final Product product; // may be null if product row not found
        public final int quantity;
        public final BigDecimal unitPrice;

        public SaleItemRecord(long productId, Product product, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.product = product;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
}
