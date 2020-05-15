package ibm.gse.orderms.infrastructure.events.voyage;

public class VoyageAssignmentPayload {

    private String orderID;
    private String voyageID;

    public VoyageAssignmentPayload(String orderID, String voyageID) {
        this.orderID = orderID;
        this.voyageID = voyageID;
    }

    public VoyageAssignmentPayload() {}

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getVoyageID() {
        return voyageID;
    }

    public void setVoyageID(String voyageID) {
        this.voyageID = voyageID;
    }

}
