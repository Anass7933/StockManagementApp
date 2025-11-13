package com.stockapp.models;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Product {
    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
    private int minStock;
    private OffsetDateTime createdAt;

    public Product(int id, String name, String description, BigDecimal price, int quantity, int minStock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.minStock = minStock;
        this.createdAt = OffsetDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getMinStock() {
        return minStock;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setPrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = newPrice;
    }

    public void increaseStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantity += amount;
    }

    public void decreaseStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > this.quantity) {
            throw new IllegalArgumentException("Not enough stock to decrease");
        }
        this.quantity -= amount;
    }

    public boolean needsRestock() {
        return this.quantity <= this.minStock;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", minStock=" + minStock +
                ", createdAt=" + createdAt +
                '}';
    }
}
