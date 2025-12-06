package com.stockapp.models.entities;

import com.stockapp.models.interfaces.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Sale implements Identifiable, Auditable {
	private long saleId;
	private BigDecimal totalPrice;
	private OffsetDateTime createdAt;
    private int totalItems;


    public Sale(long saleId, BigDecimal totalPrice, OffsetDateTime createdAt) {
		this.saleId = saleId;
		this.totalPrice = totalPrice;
		this.createdAt = createdAt;
	}

	public Sale(int saleId, BigDecimal total_price) {
		this.saleId = saleId;
		this.totalPrice = total_price;
		this.createdAt = OffsetDateTime.now();
	}

	public Sale(BigDecimal total_price) {
		this.totalPrice = total_price;
		this.createdAt = OffsetDateTime.now();
	}

	public long getId() {
		return saleId;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

    public int getTotalItems() { return totalItems; }

	public void setId(Long saleId) {
		this.saleId = saleId;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

}
