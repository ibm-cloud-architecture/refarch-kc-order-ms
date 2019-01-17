package ibm.labs.kc.order.command.dto;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import ibm.labs.kc.order.command.model.Address;

public class CreateOrderRequest {

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

    /**
     * @param co
     * @throw IllegalArgumentException
     */
    public static void validate(CreateOrderRequest co) {
        // validation
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
