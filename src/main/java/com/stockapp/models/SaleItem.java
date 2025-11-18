package com.stockapp.models;

import com.stockapp.models.Product;

public class SaleItem {
    private long saleItemId;
    private Sale sale; // optional, can be set when added to a Sale
    private Product product;
    private int quantity;
    private double unitPrice;
    private double lineTotal;

    public SaleItem(int saleItemId, Product product, int quantity, double unitPrice){
        this.saleItemId = saleItemId;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity * unitPrice;
    }

    public long getSaleItemId() {
        return saleItemId;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.lineTotal = this.quantity * this.unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    @Override
    public String toString() {
        return "SaleItem ID: " + saleItemId +
                ", Product: " + product +
                ", Quantity: " + quantity +
                ", Unit Price: $" + unitPrice +
                ", Line Total: $" + lineTotal;
    }
}
