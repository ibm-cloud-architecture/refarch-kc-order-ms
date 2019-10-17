package ibm.gse.orderqueryms.domain.model;

public class VoyageAssignment {

    private String orderID;
    private String voyageID;

    public VoyageAssignment(String orderID, String voyageID) {
        this.orderID = orderID;
        this.voyageID = voyageID;
    }

    public VoyageAssignment() {}

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
