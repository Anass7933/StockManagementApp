public class SaleItem {
    private int saleItemId;
    private Sale sale;
    private Product product;
    private int quantity;
    private double unitPrice;
    private double lineTotal;

    public SaleItem(int saleItemId, Product product, int quantity, double unitPrice){
        this.saleItemId = saleItemId;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getSaleItemId() {
        return saleItemId;
    }

    public Product getProduct() {
        return product;
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

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String toString() {
        return 0;
    }
}
