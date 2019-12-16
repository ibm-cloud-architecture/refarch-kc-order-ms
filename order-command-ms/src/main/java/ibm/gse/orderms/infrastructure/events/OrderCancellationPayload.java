package ibm.gse.orderms.infrastructure.events;

public class OrderCancellationPayload {

    private String orderID;
    private String reason;

    public OrderCancellationPayload() {}

    public OrderCancellationPayload(String orderID, String reason) {
        this.orderID = orderID;
        this.reason = reason;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
