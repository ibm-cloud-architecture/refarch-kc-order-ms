package ibm.gse.orderms.app.dto;

public class ShippingOrderReference {
	 private String orderID;
	 private String customerID;
	 private String productID;
	 private String status;
	 
	 
	public ShippingOrderReference(String orderID, String customerID, String productID, String status) {
		super();
		this.orderID = orderID;
		this.customerID = customerID;
		this.productID = productID;
		this.status = status;
	}
	
	public String getOrderID() {
		return orderID;
	}
	public String getCustomerID() {
		return customerID;
	}
	public String getProductID() {
		return productID;
	}
	public String getStatus() {
		return status;
	}
	 
}
