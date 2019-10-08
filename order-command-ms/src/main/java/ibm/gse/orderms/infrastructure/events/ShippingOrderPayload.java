package ibm.gse.orderms.infrastructure.events;

import ibm.gse.orderms.domain.model.order.Address;

/**
 * Better to have a different model for the shipping order as event payload
 * than to use the shipping order as-is.
 * 
 * This is a DTO
 * @author jerome boyer
 *
 */
public class ShippingOrderPayload {
	private String orderID;
    private String productID;
    private String customerID;
    private int quantity;

    private Address pickupAddress;
    private String pickupDate;

    private Address destinationAddress;
    private String expectedDeliveryDate;

    private String status;
    
    

	public ShippingOrderPayload(String orderID, String productID, String customerID, int quantity,
			Address pickupAddress, String pickupDate, Address destinationAddress, String expectedDeliveryDate,
			String status) {
		super();
		this.orderID = orderID;
		this.productID = productID;
		this.customerID = customerID;
		this.quantity = quantity;
		this.pickupAddress = pickupAddress;
		this.pickupDate = pickupDate;
		this.destinationAddress = destinationAddress;
		this.expectedDeliveryDate = expectedDeliveryDate;
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOrderID() {
		return orderID;
	}

	public String getProductID() {
		return productID;
	}

	public String getCustomerID() {
		return customerID;
	}

	public int getQuantity() {
		return quantity;
	}

	public Address getPickupAddress() {
		return pickupAddress;
	}

	public String getPickupDate() {
		return pickupDate;
	}

	public Address getDestinationAddress() {
		return destinationAddress;
	}

	public String getExpectedDeliveryDate() {
		return expectedDeliveryDate;
	}
    
    
}
