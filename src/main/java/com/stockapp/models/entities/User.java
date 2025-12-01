package com.stockapp.models.entities;

import com.stockapp.models.enums.UserRole;
import com.stockapp.models.interfaces.*;
import java.time.OffsetDateTime;

public class User implements Identifiable, Auditable {
	private long userId;
	private String userName, passwordHash, fullName;
	private UserRole role;
	private OffsetDateTime createdAt;

	public User() {
	}

	public User(
			long userId, String userName, String passwordHash, String fullName, UserRole role,
			OffsetDateTime createdAt) {
		this.userId = userId;
		this.userName = userName;
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.role = role;
		this.createdAt = createdAt;
	}

	public User(String userName, String passwordHash, String fullName, UserRole role, OffsetDateTime createdAt) {
		this.userName = userName;
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.role = role;
		this.createdAt = createdAt;
	}

	public long getId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getFullName() {
		return fullName;
	}

	public UserRole getRole() {
		return role;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setId(long id) {
		this.userId = id;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
