package com.stockapp.models;

import java.time.OffsetDateTime;

public class Sale {
    private long saleId;
	private long totalPrice;
    private OffsetDateTime createdAt;

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



    @Override
    public String toString() {
        return "Sale ID: " + saleId +
				"total price: " + totalPrice +
                ", Created At: " + createdAt;
    }
}
