package ibm.labs.kc.order.query.model;

public class Container {
	
	private String orderID;
    private String customerID;
    private String voyageID;
    
    public Container() {}

    public Container(String orderID, String customerID) {
        this.setOrderID(orderID);
        this.setCustomerID(customerID);
    }
    
    public Container(String orderID, String customerID, String voayageID) {
        this.setOrderID(orderID);
        this.setCustomerID(customerID);
        this.setVoyageID(voayageID);
    }
    
	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	public String getCustomerID() {
		return customerID;
	}
	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}
	public String getVoyageID() {
		return voyageID;
	}
	public void setVoyageID(String voyageID) {
		this.voyageID = voyageID;
	}
    
    

}
