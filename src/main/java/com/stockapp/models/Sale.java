package com.stockapp.models;

import java.time.OffsetDateTime;

public class Sale {
    private final long saleId;
	private final long totalPrice;
    private final OffsetDateTime createdAt;

    public Sale(int saleId,long total_price) {
        this.saleId = saleId;
		this.totalPrice = total_price;
        this.createdAt = OffsetDateTime.now();
    }

    public long getSaleId() {
        return saleId;
    }

	public long getTotalPrice() {
		return totalPrice;
	}

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

}
