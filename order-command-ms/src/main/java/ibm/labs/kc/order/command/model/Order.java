package ibm.labs.kc.order.command.model;

public class Order {

    private String orderId;
    private String productId;
    private int quantity;
    private String expectedDeliveryDate;
    private String status;

    public Order(String orderId, String productId, int quantity, String expectedDeliveryDate, String status) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
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
