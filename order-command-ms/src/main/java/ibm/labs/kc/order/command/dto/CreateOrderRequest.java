package ibm.labs.kc.order.command.dto;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import ibm.labs.kc.order.command.model.Address;

public class CreateOrderRequest {
//    pickupAddress: Address;
//    destinationAddress: Address;

    private String productID;
    private String customerID;
    private int quantity;
    private String expectedDeliveryDate;
    private Address pickupAddress;
    private Address destinationAddress;

    public String getProductID() {
        return productID;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setProductID(String productId) {
        this.productID = productId;
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

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public Address getPickupAddress() {
		return pickupAddress;
	}

	public void setPickupAddress(Address pickupAddress) {
		this.pickupAddress = pickupAddress;
	}

	public Address getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(Address destinationAddress) {
		this.destinationAddress = destinationAddress;
	}
}
