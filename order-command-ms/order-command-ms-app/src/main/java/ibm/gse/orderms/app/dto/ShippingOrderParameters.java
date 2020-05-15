package ibm.gse.orderms.app.dto;

import ibm.gse.orderms.domain.model.order.Address;

public abstract class ShippingOrderParameters {

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


}
