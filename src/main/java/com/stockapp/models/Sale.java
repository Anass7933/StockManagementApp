public class Sale {
    private int saleId;
    private User user;
    private double totalAmount;
    private OffsetDateTime createdAt;
    private List<SaleItem> items;

    public int getSaleId() {
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
        item.add(item);
    }

    public double calculateTotal(){
        return 0;
    }

    public void finalizeSale() {

    }

    public String toString() {
        return ;
    }


}