package ibm.labs.kc.order.command.dto;

public class OrderUpdate extends OrderRequest {

    private String orderID;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public static void validate(OrderUpdate co) {
        if (co.getOrderID() == null) {
            throw new IllegalArgumentException("Order ID is null");
        }
        OrderCreate.validate(co);
    }
}
