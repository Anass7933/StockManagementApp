package com.stockapp.models;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Product {

    private long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
    private int minStock;
    private OffsetDateTime createdAt;

    // Constructor without ID (used when creating new products)
    public Product(String name, String description,
                   BigDecimal price, int quantity, int minStock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.minStock = minStock;
    }

    // Constructor with ID (used when loading from DB)
    public Product(long id, String name, String description,
                   BigDecimal price, int quantity,
                   int minStock, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.minStock = minStock;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getMinStock() { return minStock; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setId(long id) { this.id = id; }
    public void setPrice(BigDecimal p) { this.price = p; }

    public boolean needsRestock() {
        return quantity <= minStock;
    }

    public void increaseStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantity += amount;
    }
}
