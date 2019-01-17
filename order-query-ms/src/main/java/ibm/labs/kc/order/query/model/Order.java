package ibm.labs.kc.order.query.model;

public class Order {
    
    public static final String CREATED_STATE = "OrderCreated";

    private String orderID;
    private String productID;
    private int quantity;
    private String expectedDeliveryDate;
    private String status;

    public Order(String orderID, String productID, int quantity, String expectedDeliveryDate, String status) {
        this.orderID = orderID;
        this.productID = productID;
        this.quantity = quantity;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getProductID() {
        return productID;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public String getStatus() {
        return status;
    }
}
