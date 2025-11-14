package com.stockapp.models;

import java.time.OffsetDateTime;

public class RestockRequest {
    private int id;
    private Product product;
    private int quantityRequested;
    private RestockStatus status;
    private OffsetDateTime createdAt;

    public RestockRequest(int id, Product product, int quantityRequested) {
        if (quantityRequested <= 0) {
            throw new IllegalArgumentException("Quantity requested must be positive");
        }
        this.id = id;
        this.product = product;
        this.quantityRequested = quantityRequested;
        this.status = RestockStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
    }

    public int getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantityRequested() {
        return quantityRequested;
    }

    public RestockStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(RestockStatus status) {
        this.status = status;
    }

    public void markFulfilled() {
        if (status == RestockStatus.FULFILLED) {
            throw new IllegalStateException("Request is already fulfilled");
        }
        this.status = RestockStatus.FULFILLED;
        product.increaseStock(quantityRequested);
    }

    @Override
    public String toString() {
        return "RestockRequest{" +
                "id=" + id +
                ", product=" + product.getName() +
                ", quantityRequested=" + quantityRequested +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
