package ibm.labs.kc.order.command.model;

import java.util.Date;

public class Order {

	private String orderID;
	private String productID;
	private int quantity;
	private Date expectedDeliveryDate;
	private String status;
	
	public Order(String orderID, String productID, int quantity, Date expectedDeliveryDate, String status) {
		this.orderID = orderID;
		this.productID = productID;
		this.quantity = quantity;
		this.expectedDeliveryDate = expectedDeliveryDate;
		this.status = status;
	}

	public String getOrderID() {
		return orderID;
	}

	public String getProductID() {
		return productID;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public Date getExpectedDeliveryDate() {
		return expectedDeliveryDate;
	}
	
	public String getStatus() {
		return status;
	}
}
