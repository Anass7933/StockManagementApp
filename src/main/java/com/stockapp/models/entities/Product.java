package com.stockapp.models.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.stockapp.models.interfaces.*;

public class Product implements Identifiable, Auditable {

	private long id;
	private String name;
	private String description;
	private BigDecimal price;
	private int quantity;
	private int minStock;
	private OffsetDateTime createdAt;
	private String category;

	// Constructor without ID (used when creating new products)
	public Product(String name, String description,
			BigDecimal price, int quantity, int minStock, String category) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.quantity = quantity;
		this.minStock = minStock;
		this.category = category;
	}

	// Constructor with ID (used when loading from DB)
	public Product(long id, String name, String description,
			BigDecimal price, int quantity,
			int minStock, OffsetDateTime createdAt, String category) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.quantity = quantity;
		this.minStock = minStock;
		this.createdAt = createdAt;
		this.category = category;
	}

	public long getId() {
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

	public String getCategory() {
		return category;
	}

	public void setID(Long id) {
		this.id = id;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
