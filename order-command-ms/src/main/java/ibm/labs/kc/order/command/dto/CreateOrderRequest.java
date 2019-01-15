package ibm.labs.kc.order.command.dto;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class CreateOrderRequest {
//    pickupAddress: Address;
//    destinationAddress: Address;
	
	private String productID;
	private int quantity;
	private String expectedDeliveryDate;
	
	public String getProductID() {
		return productID;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public String getExpectedDeliveryDate() {
		return expectedDeliveryDate;
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
	
	/**
	 * @param co
	 * @throw IllegalArgumentException
	 */
	public static void validate(CreateOrderRequest co) {
		// validation
		try {
			OffsetDateTime.parse(co.getExpectedDeliveryDate(), DateTimeFormatter.ISO_DATE_TIME);
		} catch (RuntimeException rex) {
			throw new IllegalArgumentException(rex);
		}
		
		if (co.getQuantity() <= 0) {
			throw new IllegalArgumentException("Quantity must be positive");
		}
	}
}
