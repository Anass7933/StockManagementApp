package com.stockapp.models;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private long saleId;
    private User user;
    private double totalAmount;
    private OffsetDateTime createdAt;
    private List<SaleItem> items;

    public Sale(int saleId, User user) {
        this.saleId = saleId;
        this.user = user;
        this.createdAt = OffsetDateTime.now();
        this.items = new ArrayList<>();
    }

    public long getSaleId() {
        return saleId;
    }

    public User getUser() {
        return user;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void addItem(SaleItem item){
        items.add(item);
    }

    public double calculateTotal(){
        totalAmount = 0;
        for (SaleItem item : items) {
            totalAmount += item.getLineTotal();
        }
        return totalAmount;
    }


    public void finalizeSale() {
        totalAmount = calculateTotal();
    }

    @Override
    public String toString() {
        return "Sale ID: " + saleId +
                ", User: " + user +
                ", Total: $" + totalAmount +
                ", Created At: " + createdAt +
                ", Items: " + items;
    }
}
