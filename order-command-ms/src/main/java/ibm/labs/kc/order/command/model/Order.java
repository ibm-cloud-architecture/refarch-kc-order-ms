package ibm.labs.kc.order.command.model;

public class Order {

	private String orderID;
	
	private String productID;

	public String getOrderID() {
		return orderID;
	}
	
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	
	public String getProductID() {
		return productID;
	}
	
	public void setProductID(String productID) {
		this.productID = productID;
	}
}
