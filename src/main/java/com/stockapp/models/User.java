package com.stockapp.models;
import java.time.OffsetDateTime;

public class User {
    private long userId;
    private final String userName;
    private final String passwordHash;
    private final String fullName;
    private final UserRole role;
    private final OffsetDateTime createdAt;

    // Constructor with ID (used when loading from DB to the table)
    public User(long userId, String userName, String passwordHash, String fullName, UserRole role, OffsetDateTime createdAt){
        this.userId = userId;
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;

    }

    // Constructor without ID (used when creating new users)
    public User(String userName,String passwordHash, String fullName, UserRole role, OffsetDateTime createdAt){
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
    }

    public long getUserId() { return userId; }

    public String getUserName() { return userName; }

    public String getPasswordHash() { return passwordHash; }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return role == UserRole.STOCK_MANAGER;
    }

    @Override
    public String toString() {
        return "User{id=" + userId +
                ", username='" + userName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}
