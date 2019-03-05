package ibm.labs.kc.order.query.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Order {

    public static final String PENDING_STATUS = "pending";
    public static final String CANCELLED_STATUS = "cancelled";
    public static final String ASSIGNED_STATUS = "assigned";
    public static final String BOOKED_STATUS = "booked";
    public static final String REJECTED_STATUS = "rejected";
    public static final String CONTAINER_ALLOCATED_STATUS = "container-allocated";
    public static final String FULL_CONTAINER_VOYAGE_READY_STATUS = "full-container-voyage-ready";
    public static final String CONTAINER_ON_SHIP_STATUS = "container-on-ship";
    public static final String CONTAINER_OFF_SHIP_STATUS = "container-off-ship";
    public static final String CONTAINER_DELIVERED_STATUS = "container-delivered";
    public static final String ORDER_COMPLETED_STATUS = "order-completed";
    
    private String orderID;
    private String productID;
    private String customerID;
    private int quantity;

    private Address pickupAddress;
    private String pickupDate;

    private Address destinationAddress;
    private String expectedDeliveryDate;

    private String status;

    public Order() {
    }
    
    public Order(String orderID) {
        this.orderID = orderID;
    }

    public Order(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
            String pickupDate, Address destinationAddress, String expectedDeliveryDate, String status) {
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

    public String getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public Address getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(Address destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    
}
