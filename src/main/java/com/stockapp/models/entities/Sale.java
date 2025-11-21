package com.stockapp.models.entities;

import java.time.OffsetDateTime;
import com.stockapp.models.interfaces.*;

public class Sale implements Identifiable, Auditable {
	private long saleId;
	private long totalPrice;
	private OffsetDateTime createdAt;

	public Sale(long saleId, long totalPrice, OffsetDateTime createdAt) {
		this.saleId = saleId;
		this.totalPrice = totalPrice;
		this.createdAt = createdAt;
	}

	public Sale(int saleId, long total_price) {
		this.saleId = saleId;
		this.totalPrice = total_price;
		this.createdAt = OffsetDateTime.now();
	}

	public Sale(long total_price) {
		this.totalPrice = total_price;
		this.createdAt = OffsetDateTime.now();
	}

	public long getId() {
		return saleId;
	}

	public long getTotalPrice() {
		return totalPrice;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setId(Long saleId) {
		this.saleId = saleId;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
