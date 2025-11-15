package com.stockapp.models;
import com.stockapp.models.UserRole;
import java.time.OffsetDateTime;

public class User {
    private long userId;
    private String userName, passwordHash, fullName;
    private UserRole role;
    private OffsetDateTime createdAt;

    public User(int userId, String userName, String passwordHash, String fullName, UserRole role){
        this.userId = userId;
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = OffsetDateTime.now();
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

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean verifyPassword(String plainPassword){
        return passwordHash.equals(plainPassword);
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
