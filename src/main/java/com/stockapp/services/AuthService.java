package com.stockapp.services;

import com.stockapp.models.User;
import com.stockapp.models.UserRole;
import com.stockapp.utils.DatabaseUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    /* ========== HASH PASSWORD ========== */
    public String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(plainPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    /* ========== VERIFY PASSWORD ========== */
    public boolean verify(String plainPassword, String hash) {
        return hashPassword(plainPassword).equals(hash);
    }

    /* ========== CREATE USER ========== */
    public void createUser(User user) {
        String sql = """
            INSERT INTO users (username, password_hash, full_name, user_role)
            VALUES (?, ?, ?, ?)
            RETURNING id, created_at;
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, hashPassword(user.getPasswordHash()));
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole().name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setUserId(rs.getLong("id"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /* ========== FIND USER BY USERNAME ========== */
    public User findByUsername(String username) {
        String sql = """
            SELECT id, username, password_hash, full_name, user_role, created_at
            FROM users
            WHERE username = ?
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            UserRole.valueOf(rs.getString("user_role"))
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user", e);
        }

        return null;
    }

    /* ========== AUTHENTICATE USER ========== */
    public User authenticate(String username, String password) throws SecurityException {
        User user = findByUsername(username);
        if (user == null) throw new SecurityException("User not found");
        if (!verify(password, user.getPasswordHash())) throw new SecurityException("Invalid password");
        return user;
    }
}
