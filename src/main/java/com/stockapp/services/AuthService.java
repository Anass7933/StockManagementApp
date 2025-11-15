package com.stockapp.services;

import com.stockapp.models.User;
import com.stockapp.utils.DatabaseUtils;
import com.stockapp.models.UserRole;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.time.OffsetDateTime;

public class AuthService {

    /* ========== HASH PASSWORD ========== */
    public static String hashPassword(String plainPassword) {
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

    /* ========== DELETE USER ========== */
    public void deleteUser(long id) {
        String sql = """
			DELETE FROM users where id = ?
        """;

        try (Connection c = DatabaseUtils.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1,id);
			ps.executeQuery();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /* ========== AUTHENTICATE USER ========== */

    public static boolean validateLogin(String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }


	public static List<User> loadUsers() throws SQLException {
		String sql = "SELECT id, full_name, username, password_hash, role, created_at FROM users WHERE username != 'admin'";

		List<User> userList = new ArrayList<>();

		try (Connection conn = DatabaseUtils.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				int id = rs.getInt("id"); // cast to int
				String fullName = rs.getString("full_name");
				String username = rs.getString("username");
				String passwordHash = rs.getString("password_hash");
				UserRole role = UserRole.valueOf(rs.getString("role"));
				OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);

				User user = new User(id, username, passwordHash, fullName, role, createdAt);
				userList.add(user);
			}
		}

		return userList;
	}
}
