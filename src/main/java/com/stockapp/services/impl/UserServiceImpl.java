package com.stockapp.services.impl;

import com.stockapp.models.entities.User;
import com.stockapp.services.interfaces.UserService;
import com.stockapp.utils.*;
import com.stockapp.models.enums.UserRole;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
	public User create(User user) {
		String sql_query = """
					INSERT INTO users (username, password_hash, full_name,role)
					VALUES (?, ?, ?, ?::user_role)
					RETURNING id, created_at;
				""";
		try (Connection c = DatabaseUtils.getConnection();) {
			PreparedStatement ps = c.prepareStatement(sql_query);
			ps.setString(1, user.getUserName());
			ps.setString(2, user.getPasswordHash());
			ps.setString(3, user.getFullName());
			ps.setString(4, user.getRole().name());

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				user.setId(rs.getLong("id"));
				user.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
			}
			return user;

		} catch (SQLException e) {
			throw new RuntimeException("Error creating user", e);
		}

	}

	public Optional<User> read(Long id) {
		String sql_query = "SELECT id, username, password_hash, full_name, role, created_at FROM users WHERE id = ?;";
		try (Connection c = DatabaseUtils.getConnection();) {
			PreparedStatement ps = c.prepareStatement(sql_query);
			ps.setLong(1, id);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				User user = new User(
						rs.getLong("id"),
						rs.getString("username"),
						rs.getString("password_hash"),
						rs.getString("full_name"),
						UserRole.valueOf(rs.getString("role")),
						rs.getObject("created_at", OffsetDateTime.class));
				return Optional.of(user);
			} else {
				return Optional.empty();
			}

		} catch (SQLException e) {
			throw new RuntimeException("Error reading user", e);
		}
	}

	public User update(User user) {
		String sql_query = """
					UPDATE users
					SET username = ?, password_hash = ?, full_name = ?, role = ?
					WHERE id = ?;
				""";
		try (Connection c = DatabaseUtils.getConnection();) {
			PreparedStatement ps = c.prepareStatement(sql_query);
			ps.setString(1, user.getUserName());
			ps.setString(2, user.getPasswordHash());
			ps.setString(3, user.getFullName());
			ps.setString(4, user.getRole().name());
			ps.setLong(5, user.getId());

			ps.executeUpdate();
			return user;

		} catch (SQLException e) {
			throw new RuntimeException("Error updating user", e);
		}
	}

	public void delete(Long id) {
		String sql_query = "DELETE FROM users WHERE id = ?;";
		try (Connection c = DatabaseUtils.getConnection();) {
			PreparedStatement ps = c.prepareStatement(sql_query);
			ps.setLong(1, id);

			ps.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("Error deleting user", e);
		}
	}

	public List<User> readAll() {
		String sql_query = "SELECT id, username, password_hash, full_name, role, created_at FROM users;";
		List<User> users = new ArrayList<>();
		try (Connection c = DatabaseUtils.getConnection();) {
			PreparedStatement ps = c.prepareStatement(sql_query);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				User user = new User(
						rs.getLong("id"),
						rs.getString("username"),
						rs.getString("password_hash"),
						rs.getString("full_name"),
						UserRole.valueOf(rs.getString("role")),
						rs.getObject("created_at", OffsetDateTime.class));
				users.add(user);
			}
			return users;

		} catch (SQLException e) {
			throw new RuntimeException("Error reading all users", e);
		}
	}

	public List<User> findByRole(UserRole role) {
		String sql_query = "SELECT id, username, password_hash, full_name, role, created_at FROM users WHERE role = ?;";
		List<User> users = new ArrayList<>();
		try (Connection c = DatabaseUtils.getConnection();) {
			PreparedStatement ps = c.prepareStatement(sql_query);
			ps.setString(1, role.name());

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				User user = new User(
						rs.getLong("id"),
						rs.getString("username"),
						rs.getString("password_hash"),
						rs.getString("full_name"),
						UserRole.valueOf(rs.getString("role")),
						rs.getObject("created_at", OffsetDateTime.class));
				users.add(user);
			}
			return users;

		} catch (SQLException e) {
			throw new RuntimeException("Error finding users by role", e);
		}
	}

	public User findByUsername(String userName) {
		String sql_query = "SELECT id, username, password_hash, full_name, role, created_at FROM users WHERE username = ?;";
		try (Connection c = DatabaseUtils.getConnection();) {
			PreparedStatement ps = c.prepareStatement(sql_query);
			ps.setString(1, userName);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return new User(
						rs.getLong("id"),
						rs.getString("username"),
						rs.getString("password_hash"),
						rs.getString("full_name"),
						UserRole.valueOf(rs.getString("role")),
						rs.getObject("created_at", OffsetDateTime.class));
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException("Error finding users by username", e);
		}
	}

}
