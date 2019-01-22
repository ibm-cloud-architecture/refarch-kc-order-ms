package ibm.labs.kc.order.command.dto;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import ibm.labs.kc.order.command.model.Address;

public class OrderRequest {

    private String customerID;
    private String productID;
    private int quantity;
    private String expectedDeliveryDate;
    private String pickupDate;
    private Address pickupAddress;
    private Address destinationAddress;

    public String getProductID() {
        return productID;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public String getPickupDate() {
        return pickupDate;
    }

    public Address getPickupAddress() {
        return pickupAddress;
    }

    public Address getDestinationAddress() {
        return destinationAddress;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setExpectedDeliveryDate(String expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    public void setPickupAddress(Address pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public void setDestinationAddress(Address destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public static void validate(OrderRequest co) {
        if (co.getProductID() == null) {
            throw new IllegalArgumentException("Product ID is null");
        }
        if (co.getCustomerID() == null) {
            throw new IllegalArgumentException("Customer ID is null");
        }
        if (co.getExpectedDeliveryDate() == null) {
            throw new IllegalArgumentException("Expected delivery date is null");
        }
        if (co.getPickupDate() == null) {
            throw new IllegalArgumentException("Pickup date is null");
        }
        if (co.getPickupAddress() == null) {
            throw new IllegalArgumentException("Pickup address is null");
        }
        if (co.getDestinationAddress() == null) {
            throw new IllegalArgumentException("Destination address is null");
        }

        try {
            OffsetDateTime.parse(co.getExpectedDeliveryDate(), DateTimeFormatter.ISO_DATE_TIME);
            OffsetDateTime.parse(co.getPickupDate(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (RuntimeException rex) {
            throw new IllegalArgumentException(rex);
        }

        if (co.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

}
