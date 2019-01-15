package ibm.labs.kc.order.command.dto;

public class CreateOrderRequest {
//    pickupAddress: Address;
//    destinationAddress: Address;
	
	private String productID;
	private int quantity;
	private String expectedDeliveryDate;
	private String status;
	
	public String getProductID() {
		return productID;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public String getExpectedDeliveryDate() {
		return expectedDeliveryDate;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setProductID(String productID) {
		this.productID = productID;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public void setExpectedDeliveryDate(String expectedDeliveryDate) {
		this.expectedDeliveryDate = expectedDeliveryDate;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
}
