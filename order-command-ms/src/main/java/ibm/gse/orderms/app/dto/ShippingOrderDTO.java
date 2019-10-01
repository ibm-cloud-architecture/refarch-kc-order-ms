package ibm.gse.orderms.app.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ibm.gse.orderms.domain.model.order.Address;
import ibm.gse.orderms.domain.model.order.ShippingOrder;

public class ShippingOrderDTO {

    private String orderID;
    private String productID;
    private String customerID;
    private int quantity;
    private Address pickupAddress;
    private String pickupDate;
    private Address destinationAddress;
    private String expectedDeliveryDate;
    private String status;

    public ShippingOrderDTO(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
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

    public static ShippingOrderDTO newFromOrder(ShippingOrder order) {
        return new ShippingOrderDTO(order.getOrderID(),
                order.getProductID(), order.getCustomerID(), order.getQuantity(),
                order.getPickupAddress(), order.getPickupDate(),
                order.getDestinationAddress(), order.getExpectedDeliveryDate(),
                order.getStatus());
    }

    public void update(ShippingOrder order) {
        if (!ShippingOrder.PENDING_STATUS.contentEquals(status)) {
            throw new IllegalStateException(
                    "Unable to update a QueryOrder not in " + ShippingOrder.PENDING_STATUS + " state");
        }
        if (order.getCustomerID() != null) {
            customerID = order.getCustomerID();
        }
        if (order.getProductID() != null) {
            productID = order.getProductID();
        }
        if (order.getQuantity() != 0) {
            quantity = order.getQuantity();
        }
        if (order.getPickupAddress() != null) {
            pickupAddress = order.getPickupAddress();
        }
        if (order.getPickupDate() != null) {
            pickupDate = order.getPickupDate();
        }
        if (order.getDestinationAddress() != null) {
            destinationAddress = order.getDestinationAddress();
        }
        if (order.getExpectedDeliveryDate() != null) {
            expectedDeliveryDate = order.getExpectedDeliveryDate();
        }
    }

    

    public String getStatus() {
        return status;
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

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
