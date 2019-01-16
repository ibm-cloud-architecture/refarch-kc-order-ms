package ibm.labs.kc.order.query.model;

public class Order {

    private String orderID;
    private String productID;
    private int quantity;
    private String expectedDeliveryDate;
    private String status;

    public Order(String orderId, String productID, int quantity, String expectedDeliveryDate, String status) {
        this.orderID = orderId;
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
