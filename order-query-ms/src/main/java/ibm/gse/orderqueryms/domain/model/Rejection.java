package ibm.gse.orderqueryms.domain.model;

public class Rejection {
	
	private String orderID;
    private String customerID;
    
    public Rejection() {}

    public Rejection(String orderID, String customerID) {
        this.setOrderID(orderID);
        this.setCustomerID(customerID);
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
    
}
