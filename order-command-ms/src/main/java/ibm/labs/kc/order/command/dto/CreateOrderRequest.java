package ibm.labs.kc.order.command.dto;

public class CreateOrderRequest {
//    pickupAddress: Address;
//    destinationAddress: Address;
//    productID: string;
//    quantity: string;
//    expectedDeliveryDate: string;
//    status: string;
	
	private String productID;
	
	public String getProductID() {
		return productID;
	}
	
	public void setProductID(String productID) {
		this.productID = productID;
	}
}
