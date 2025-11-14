package com.stockapp.services;

import com.stockapp.models.Product;
import com.stockapp.models.RestockRequest;
import com.stockapp.models.RestockStatus;
import com.stockapp.models.User;
import com.stockapp.models.UserRole;
import com.stockapp.utils.DatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestockService {

    private final ProductService productService;

    public RestockService(ProductService productService) {
        this.productService = productService;
    }


    /* -------------------------
       CREATE RESTOCK REQUEST
       ------------------------- */
    public RestockRequest createRestockRequest(User user, long productId, int quantityRequested) {
        // Allowed: STOCK_MANAGER and CASHIER (they can request restock)
        AuthService.requireRole(user, UserRole.STOCK_MANAGER, UserRole.CASHIER);

        if (quantityRequested <= 0) {
            throw new IllegalArgumentException("Quantity requested must be positive");
        }

        String sql = """
            INSERT INTO restock_requests (product_id, quantity_requested, status)
            VALUES (?, ?, ?)
            RETURNING id, created_at;
            """;

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, productId);
            ps.setInt(2, quantityRequested);
            ps.setString(3, RestockStatus.PENDING.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    // load product via ProductService
                    Product product = productService.getProductById(productId);
                    if (product == null) {
                        throw new RuntimeException("Product with id " + productId + " not found");
                    }
                    // RestockRequest constructor uses (int id, Product, int qty)
                    RestockRequest rr = new RestockRequest((int) id, product, quantityRequested);
                    // set status explicitly if needed (constructor already sets PENDING)
                    rr.setStatus(RestockStatus.PENDING);
                    return rr;
                } else {
                    throw new RuntimeException("Failed to create restock request");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error while creating restock request", e);
        }
    }

    /* -------------------------
       GET ALL RESTOCK REQUESTS
       ------------------------- */
    public List<RestockRequest> getRestockRequests(User user) {
        // Allowed: all roles (Stock Manager, Supervisor, Cashier)
        AuthService.requireRole(user, UserRole.STOCK_MANAGER, UserRole.SUPERVISOR, UserRole.CASHIER);

        String sql = "SELECT id, product_id, quantity_requested, status FROM restock_requests ORDER BY created_at DESC";
        List<RestockRequest> list = new ArrayList<>();

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                long productId = rs.getLong("product_id");
                int qty = rs.getInt("quantity_requested");
                String statusStr = rs.getString("status");

                Product product = productService.getProductById(productId);
                if (product == null) {
                    // skip or throw — here we throw because data integrity expected
                    throw new RuntimeException("Product with id " + productId + " not found");
                }

                RestockRequest rr = new RestockRequest((int) id, product, qty);
                rr.setStatus(RestockStatus.valueOf(statusStr));
                list.add(rr);
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching restock requests", e);
        }
    }

    /* -------------------------
       GET PENDING RESTOCKS
       ------------------------- */
    public List<RestockRequest> getPendingRestocks(User user) {
        // Allowed: Stock Manager and Supervisor (monitor pending)
        AuthService.requireRole(user, UserRole.STOCK_MANAGER, UserRole.SUPERVISOR);

        String sql = "SELECT id, product_id, quantity_requested, status FROM restock_requests WHERE status = ? ORDER BY created_at DESC";
        List<RestockRequest> list = new ArrayList<>();

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, RestockStatus.PENDING.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    long productId = rs.getLong("product_id");
                    int qty = rs.getInt("quantity_requested");

                    Product product = productService.getProductById(productId);
                    if (product == null) {
                        throw new RuntimeException("Product with id " + productId + " not found");
                    }

                    RestockRequest rr = new RestockRequest((int) id, product, qty);
                    rr.setStatus(RestockStatus.PENDING);
                    list.add(rr);
                }
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching pending restock requests", e);
        }
    }

    /* -------------------------
       GET RESTOCK BY ID
       ------------------------- */
    public RestockRequest getRestockById(User user, long requestId) {
        AuthService.requireRole(user, UserRole.STOCK_MANAGER, UserRole.SUPERVISOR, UserRole.CASHIER);

        String sql = "SELECT id, product_id, quantity_requested, status FROM restock_requests WHERE id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    long productId = rs.getLong("product_id");
                    int qty = rs.getInt("quantity_requested");
                    String statusStr = rs.getString("status");

                    Product product = productService.getProductById(productId);
                    if (product == null) {
                        throw new RuntimeException("Product with id " + productId + " not found");
                    }

                    RestockRequest rr = new RestockRequest((int) id, product, qty);
                    rr.setStatus(RestockStatus.valueOf(statusStr));
                    return rr;
                } else {
                    throw new RuntimeException("Restock request with id " + requestId + " not found");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching restock request", e);
        }
    }

    /* -------------------------
       UPDATE RESTOCK STATUS (including FULFILL logic)
       ------------------------- */
    public RestockRequest updateRestockRequestStatus(User user, long requestId, RestockStatus newStatus) {
        // Allowed: STOCK_MANAGER and SUPERVISOR
        AuthService.requireRole(user, UserRole.STOCK_MANAGER, UserRole.SUPERVISOR);

        // Fetch existing request
        RestockRequest existing = getRestockById(user, requestId);
        if (existing == null) {
            throw new RuntimeException("Restock request not found");
        }

        RestockStatus oldStatus = existing.getStatus();
        if (oldStatus == newStatus) {
            return existing; // nothing to do
        }

        // If fulfilling: update product stock and request status in a transaction
        if (newStatus == RestockStatus.FULFILLED) {
            String updateRequestSql = "UPDATE restock_requests SET status = ? WHERE id = ?";
            String updateProductSql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";

            try (Connection conn = DatabaseUtils.getConnection();
                 PreparedStatement psUpdateProduct = conn.prepareStatement(updateProductSql);
                 PreparedStatement psUpdateRequest = conn.prepareStatement(updateRequestSql)) {

                conn.setAutoCommit(false);

                // 1) update product stock
                psUpdateProduct.setInt(1, existing.getQuantityRequested());
                psUpdateProduct.setLong(2, existing.getProduct().getId());
                int updatedRows = psUpdateProduct.executeUpdate();
                if (updatedRows == 0) {
                    conn.rollback();
                    throw new RuntimeException("Failed to update product stock. Product id: " + existing.getProduct().getId());
                }

                // 2) update request status
                psUpdateRequest.setString(1, RestockStatus.FULFILLED.name());
                psUpdateRequest.setLong(2, requestId);
                int reqUpdated = psUpdateRequest.executeUpdate();
                if (reqUpdated == 0) {
                    conn.rollback();
                    throw new RuntimeException("Failed to update restock request status for id: " + requestId);
                }

                conn.commit();

                // Update in-memory object and return fresh copy
                existing.setStatus(RestockStatus.FULFILLED);
                // optional: refresh product from DB so its stock matches truth
                Product refreshed = productService.getProductById(existing.getProduct().getId());
                existing = new RestockRequest((int) requestId, refreshed, existing.getQuantityRequested());
                existing.setStatus(RestockStatus.FULFILLED);
                return existing;

            } catch (SQLException e) {
                throw new RuntimeException("Database error while fulfilling restock request", e);
            }
        } else {
            // For status changes that do not affect product stock (e.g., REJECTED or PENDING)
            String sql = "UPDATE restock_requests SET status = ? WHERE id = ?";
            try (Connection conn = DatabaseUtils.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, newStatus.name());
                ps.setLong(2, requestId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new RuntimeException("Failed to update restock request status");
                }
                existing.setStatus(newStatus);
                return existing;

            } catch (SQLException e) {
                throw new RuntimeException("Database error while updating restock request status", e);
            }
        }
    }
}
