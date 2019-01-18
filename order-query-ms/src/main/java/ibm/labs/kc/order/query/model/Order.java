package ibm.labs.kc.order.query.model;

public class Order {

	private String orderID;
    private String productID;
    private String customerID;
    private int quantity;
    private String expectedDeliveryDate;
    private String status;
    private Address pickupAddress;
    private Address destinationAddress;

    public Order(String orderId, String productId, int quantity, String expectedDeliveryDate, String status, String customerId) {
        this.orderID = orderId;
        this.productID = productId;
        this.quantity = quantity;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
        this.customerID = customerId;
    }
    
    public String toString() {
    	return getOrderID()+ " " +  getCustomerID() + " " + getProductID();
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

    public String getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public String getStatus() {
        return status;
    }

	public String getCustomerID() {
		return customerID;
	}

	public Address getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(Address destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public void setExpectedDeliveryDate(String expectedDeliveryDate) {
		this.expectedDeliveryDate = expectedDeliveryDate;
	}

	public Address getPickupAddress() {
		return pickupAddress;
	}

	public void setPickupAddress(Address pickupAddress) {
		this.pickupAddress = pickupAddress;
	}
}
