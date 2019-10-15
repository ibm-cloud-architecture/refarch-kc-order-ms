package ibm.gse.orderqueryms.domain.model;

public class Cancellation {

    private String orderID;
    private String reason;

    public Cancellation() {}

    public Cancellation(String orderID, String reason) {
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
