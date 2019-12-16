package ibm.gse.orderms.domain.model.order;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ibm.gse.orderms.infrastructure.events.OrderCancellationPayload;
import ibm.gse.orderms.infrastructure.events.OrderRejectPayload;
import ibm.gse.orderms.infrastructure.events.ShippingOrderPayload;
import ibm.gse.orderms.infrastructure.events.reefer.ReeferAssignmentPayload;
import ibm.gse.orderms.infrastructure.events.voyage.VoyageAssignmentPayload;

public class ShippingOrder {

    public static final String PENDING_STATUS = "pending";
    public static final String CANCELLED_STATUS = "cancelled";
    public static final String ASSIGNED_STATUS = "assigned";
    public static final String BOOKED_STATUS = "booked";
    public static final String TRANSIT_STATUS = "transit";
    public static final String REJECTED_STATUS = "rejected";
    public static final String COMPLETED_STATUS = "completed";
    public static final String SPOILT_STATUS = "spoilt";

    public static final String FULL_CONTAINER_VOYAGE_READY_STATUS = "full-container-voyage-ready";
    public static final String CONTAINER_ON_SHIP_STATUS = "container-on-ship";
    public static final String CONTAINER_OFF_SHIP_STATUS = "container-off-ship";
    public static final String CONTAINER_DELIVERED_STATUS = "container-delivered";
    
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
	private String reeferID;

    public ShippingOrder() {
    }

    public ShippingOrder(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
            String pickupDate, Address destinationAddress, String expectedDeliveryDate, String status) {
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

    public void assign(VoyageAssignmentPayload voyageAssignment) {
        this.voyageID = voyageAssignment.getVoyageID();
        setAssignStatus();
    }

    public void setAssignStatus() {
    	if (this.voyageID != null && this.reeferID != null) {
    		this.status = ShippingOrder.ASSIGNED_STATUS;
    	}
    }
    public void assignContainer(ReeferAssignmentPayload reeferAssignment) {
    	this.reeferID = reeferAssignment.getContainerID();
    	setAssignStatus();
    }
    
    public void cancel(OrderCancellationPayload cancellation) {
        this.status = ShippingOrder.CANCELLED_STATUS;
        this.reeferID = "";
        this.voyageID = "";
    }
    
    
    public ShippingOrderPayload toShippingOrderPayload() {
    	ShippingOrderPayload sop = new ShippingOrderPayload(this.getOrderID(),
    			this.getProductID(),
    			this.getCustomerID(),
    			this.getQuantity(),
    			this.getPickupAddress(),
    			this.getPickupDate(),
    			this.getDestinationAddress(),
    			this.getExpectedDeliveryDate(),
    			this.getStatus()
    			);
    	return sop;
    }
    
    public ShippingOrder fromShippingOrderPayload(ShippingOrderPayload sop) {
    	ShippingOrder order = new ShippingOrder(sop.getOrderID(),
    			sop.getProductID(),
    			sop.getCustomerID(),
    			sop.getQuantity(),
    			sop.getPickupAddress(),
    			sop.getPickupDate(),
    			sop.getDestinationAddress(),
    			sop.getExpectedDeliveryDate(),
    			sop.getStatus()
    			);
    	return order;
    }

    public OrderRejectPayload toOrderRejectPayload(String reason) {
    	OrderRejectPayload sop = new OrderRejectPayload(this.getOrderID(),
    			this.getProductID(),
                this.getCustomerID(),
                this.getReeferID(),
                this.getVoyageID(),
    			this.getQuantity(),
    			this.getPickupAddress(),
    			this.getPickupDate(),
    			this.getDestinationAddress(),
    			this.getExpectedDeliveryDate(),
                this.getStatus(),
                reason
    			);
    	return sop;
    }

    public void spoilOrder(){
        this.status = ShippingOrder.SPOILT_STATUS;
    }

    public void rejectOrder(){
        this.status = ShippingOrder.REJECTED_STATUS;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

	public void setOrderID(String oid) {
		this.orderID = 	oid;
	}

	public void setQuantity(int value) {
		this.quantity = value;
	}

	public String getVoyageID() {
		return voyageID;
	}

	public String getReeferID() {
		return reeferID;
	}
}
