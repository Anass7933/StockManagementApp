package com.stockapp.services.impl;

import com.stockapp.models.entities.User;
import com.stockapp.utils.*;
import com.stockapp.models.enums.UserRole;
import com.stockapp.services.interfaces.AuthService;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Optional;

public class AuthServiceImpl implements AuthService {
	public User validateLogin(String username, String password) {
		String sql_query = "SELECT id, username, password_hash, full_name, role, created_at FROM users WHERE username = ?;";
		try (Connection c = DatabaseUtils.getConnection();) {
			PreparedStatement ps = c.prepareStatement(sql_query);
			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String storedHash = rs.getString("password_hash");
				if (PasswordUtils.verifyPassword(password, storedHash)) {
					User user = new User(
							rs.getLong("id"),
							rs.getString("username"),
							storedHash,
							rs.getString("full_name"),
							UserRole.valueOf(rs.getString("role")),
							rs.getObject("created_at", OffsetDateTime.class));
					return user;
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error during login", e);
		}
	}
}
