package com.stockapp.models.entities;

import com.stockapp.models.interfaces.Identifiable;

public class SaleItem implements Identifiable {
	private long saleItemId;
	private long saleId;
	private long productId;
	private int quantity;
	private double unitPrice;
	private double lineTotal;

	public SaleItem() {

	}

	public SaleItem(long saleItemId, long productId, long saleId, int quantity, double unitPrice, double lineTotal) {
		this.saleItemId = saleItemId;
		this.productId = productId;
		this.saleId = saleId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.lineTotal = lineTotal;
	}

	public SaleItem(long saleItemId, long productId, long saleId, int quantity, double unitPrice) {
		this.saleItemId = saleItemId;
		this.productId = productId;
		this.saleId = saleId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.lineTotal = quantity * unitPrice;
	}

	public SaleItem(long productId, long saleId, int quantity, double unitPrice) {
		this.productId = productId;
		this.saleId = saleId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.lineTotal = quantity * unitPrice;
	}

	public long getId() {
		return saleItemId;
	}

	public long getSaleId() {
		return saleId;
	}

	public long getProductId() {
		return productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getUnitPrice() {
		return unitPrice;
	}

	public double getLineTotal() {
		return lineTotal;
	}

	public void setId(long saleId) {
		this.saleId = saleId;
	}

	public void setSaleId(long saleId) {
		this.saleId = saleId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public void setLineTotal(double lineTotal) {
		this.lineTotal = lineTotal;
	}

	public void setProduct(Product product) {
		this.productId = product.getId();
	}

}
