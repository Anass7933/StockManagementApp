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
                RETURNING id, created_at
                """;
        try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, sale.getTotalPrice());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sale.setId(rs.getLong("id"));
                    sale.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC));
                    return sale;
                }
                throw new RuntimeException("Failed to insert sale");
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
                    Sale sale = new Sale(
                            rs.getLong("id"),
                            rs.getBigDecimal("total_price"),
                            rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC)
                    );

                    sale.setTotalItems(getTotalItemsForSale(id));

                    return Optional.of(sale);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read sale " + id, e);
        }
    }

    @Override
    public Sale update(Sale sale) {
        String sql = """
                UPDATE sales
                SET total_price = ?
                WHERE id = ?
                RETURNING created_at
                """;
        try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, sale.getTotalPrice());
            ps.setLong(2, sale.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sale.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC));
                    return sale;
                }
                throw new RuntimeException("Failed to update sale id=" + sale.getId());
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
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("Sale not found");
        } catch (SQLException e) {
            throw new RuntimeException("Delete failed", e);
        }
    }

    @Override
    public List<Sale> readAll() {
        String sql = "SELECT id, total_price, created_at FROM sales ORDER BY created_at DESC";
        List<Sale> list = new ArrayList<>();
        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Sale sale = new Sale(
                        rs.getLong("id"),
                        rs.getBigDecimal("total_price"),
                        rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC)
                );

                sale.setTotalItems(getTotalItemsForSale(sale.getId()));

                list.add(sale);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read all sales", e);
        }
        return list;
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

            // INSERT SALE
            String sqlSale = "INSERT INTO sales (total_price) VALUES (?) RETURNING id, created_at";
            psSale = c.prepareStatement(sqlSale);
            psSale.setBigDecimal(1, sale.getTotalPrice());
            ResultSet rsSale = psSale.executeQuery();
            if (rsSale.next()) {
                sale.setId(rsSale.getLong("id"));
                sale.setCreatedAt(rsSale.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC));
            }

            // INSERT ITEMS
            String sqlItem = """
                INSERT INTO sale_items (sale_id, product_id, quantity, unit_price)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;
            psItem = c.prepareStatement(sqlItem);

            // UPDATE STOCK
            String sqlStock = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
            psStock = c.prepareStatement(sqlStock);

            for (SaleItem item : items) {

                psItem.setLong(1, sale.getId());
                psItem.setLong(2, item.getProductId());
                psItem.setInt(3, item.getQuantity());
                psItem.setBigDecimal(4, item.getUnitPrice());

                ResultSet rsItem = psItem.executeQuery();
                if (rsItem.next()) item.setId(rsItem.getLong("id"));

                psStock.setInt(1, item.getQuantity());
                psStock.setLong(2, item.getProductId());
                psStock.executeUpdate();
            }

            c.commit();
            return sale;

        } catch (Exception e) {
            try { if (c != null) c.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Failed to create sale with items", e);

        } finally {
            closeQuietly(psSale);
            closeQuietly(psItem);
            closeQuietly(psStock);
            try { if (c != null) c.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    @Override public Long getTotalRevenue(Long saleId) {
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
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to get total revenue for sale ID: " + saleId, e);
        }
    }

    private void closeQuietly(AutoCloseable r) {
        try { if (r != null) r.close(); } catch (Exception ignored) {}
    }

    private int getTotalItemsForSale(long saleId) {
        String sql = "SELECT COALESCE(SUM(quantity),0) FROM sale_items WHERE sale_id = ?";
        try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, saleId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to sum items for sale " + saleId, e);
        }
    }

    public int totalSales() {
        return simpleIntQuery("SELECT COUNT(*) FROM sales");
    }

    public long totalRevenue() {
        return simpleLongQuery("SELECT COALESCE(SUM(total_price),0) FROM sales");
    }

    public int totalItemsSold() {
        return simpleIntQuery("""
            SELECT COALESCE(SUM(quantity),0)
            FROM sale_items
        """);
    }

    public int averageSaleValue() {
        return simpleIntQuery("""
            SELECT COALESCE(AVG(total_price),0)
            FROM sales
        """);
    }

    private int simpleIntQuery(String sql) {
        try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Query failed", e);
        }
    }

    private long simpleLongQuery(String sql) {
        try (Connection c = DatabaseUtils.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("Query failed", e);
        }
    }

}
