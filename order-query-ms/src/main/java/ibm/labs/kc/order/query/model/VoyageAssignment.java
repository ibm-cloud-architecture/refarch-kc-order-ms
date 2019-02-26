package ibm.labs.kc.order.query.model;

public class VoyageAssignment {

    private String orderID;
    private String voyageID;
    private String customerID;
    private String ship;

    public VoyageAssignment(String orderID, String voyageID, String customerID, String ship) {
        this.orderID = orderID;
        this.voyageID = voyageID;
        this.setCustomerID(customerID);
        this.setShip(ship);
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

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public String getShip() {
		return ship;
	}

	public void setShip(String ship) {
		this.ship = ship;
	}

}
