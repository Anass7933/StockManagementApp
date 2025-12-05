package com.stockapp.models.entities;

import com.stockapp.models.interfaces.Identifiable;

import java.math.BigDecimal;
import java.util.Objects;

public class SaleItem implements Identifiable {
	private long saleItemId;
	private long saleId;
	private long productId;
	private int quantity;
	private BigDecimal unitPrice;
	private BigDecimal lineTotal;

	public SaleItem() {
	}

	public SaleItem(long saleItemId, long saleId, long productId, int quantity, BigDecimal unitPrice,
			BigDecimal lineTotal) {
		this.saleItemId = saleItemId;
		this.saleId = saleId;
		this.productId = productId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.lineTotal = lineTotal;
	}

	public SaleItem(long saleItemId, long productId, long saleId, int quantity, BigDecimal unitPrice) {
		this.saleItemId = saleItemId;
		this.productId = productId;
		this.saleId = saleId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	public SaleItem(long productId, long saleId, int quantity, BigDecimal unitPrice) {
		this.productId = productId;
		this.saleId = saleId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
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

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}

	public void setId(long saleItemId) {
		this.saleItemId = saleItemId;
	}

	public void setSaleId(long saleId) {
		this.saleId = saleId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
		this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
		this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(this.quantity));
	}

	public void setLineTotal(BigDecimal lineTotal) {
		this.lineTotal = lineTotal;
	}

	public void setProduct(Product product) {
		this.productId = product.getId();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SaleItem other))
			return false;
		return productId == other.productId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(productId);
	}
}
