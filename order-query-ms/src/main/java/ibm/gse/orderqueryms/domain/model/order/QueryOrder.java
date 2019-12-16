package ibm.gse.orderqueryms.domain.model.order;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ibm.gse.orderqueryms.domain.model.Address;
import ibm.gse.orderqueryms.domain.model.Cancellation;
import ibm.gse.orderqueryms.domain.model.ContainerAssignment;
import ibm.gse.orderqueryms.domain.model.Order;
import ibm.gse.orderqueryms.domain.model.Rejection;
import ibm.gse.orderqueryms.domain.model.Spoil;
import ibm.gse.orderqueryms.domain.model.VoyageAssignment;

public class QueryOrder {

    private String orderID;
    private String productID;
    private String customerID;
    private int quantity;
    private Address pickupAddress;
    private String pickupDate;
    private Address destinationAddress;
    private String expectedDeliveryDate;
    private String status;
    private String voyageID;
    private String containerID;
    private String reason;

    public QueryOrder(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
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
    
    public QueryOrder(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
            String pickupDate, Address destinationAddress, String expectedDeliveryDate, String status, String voyageID) {
        this.orderID = orderID;
        this.productID = productID;
        this.customerID = customerID;
        this.quantity = quantity;
        this.pickupAddress = pickupAddress;
        this.pickupDate = pickupDate;
        this.destinationAddress = destinationAddress;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
        this.voyageID = voyageID;
    }
    
    public QueryOrder(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
            String pickupDate, Address destinationAddress, String expectedDeliveryDate, String status, String voyageID, String containerID) {
        this.orderID = orderID;
        this.productID = productID;
        this.customerID = customerID;
        this.quantity = quantity;
        this.pickupAddress = pickupAddress;
        this.pickupDate = pickupDate;
        this.destinationAddress = destinationAddress;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
        this.voyageID = voyageID;
        this.containerID = containerID;
    }

    public static QueryOrder newFromOrder(Order order) {
        return new QueryOrder(order.getOrderID(),
                order.getProductID(), order.getCustomerID(), order.getQuantity(),
                order.getPickupAddress(), order.getPickupDate(),
                order.getDestinationAddress(), order.getExpectedDeliveryDate(),
                order.getStatus());
    }

    public void update(Order order) {
        if (!Order.PENDING_STATUS.contentEquals(status)) {
            throw new IllegalStateException(
                    "Unable to update a QueryOrder not in " + Order.PENDING_STATUS + " state");
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
        if (order.getVoyageID() != null) {
            voyageID = order.getVoyageID();
        }
        if (order.getContainerID() != null) {
        	containerID = order.getContainerID();
        }
    }

    public void assign(VoyageAssignment voyageAssignment) {
        this.voyageID = voyageAssignment.getVoyageID();
        this.status = Order.ASSIGNED_STATUS;
    }

    public void assignContainer(ContainerAssignment ca) {
    	this.containerID = ca.getContainerID();
    	this.status = Order.CONTAINER_ALLOCATED_STATUS;
    }
    
    public void cancel(Cancellation cancellation) {
        this.status = Order.CANCELLED_STATUS;
        this.reason = cancellation.getReason();
    }
    
    public void reject(Rejection rejection){
        this.status = Order.REJECTED_STATUS;
        this.reason = rejection.getReason();
    }
    
    public void containerDelivered(ContainerAssignment container){
    	this.status = Order.CONTAINER_DELIVERED_STATUS;
    }
    
    public void orderCompleted(Order order){
    	this.status = Order.ORDER_COMPLETED_STATUS;
    }

    public void spoilOrder(Spoil spoil){
    	this.status = Order.SPOILT_STATUS;
    }

    public String getStatus() {
        return status;
    }

    public String getVoyageID() {
        return voyageID;
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

    public String getReason() {
        return reason;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}

	public void setVoyageID(String voyageID) {
		this.voyageID = voyageID;
	}
}
