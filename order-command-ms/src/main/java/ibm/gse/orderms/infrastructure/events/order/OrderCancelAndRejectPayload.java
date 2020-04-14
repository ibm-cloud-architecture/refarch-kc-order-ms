package ibm.gse.orderms.infrastructure.events.order;

import ibm.gse.orderms.domain.model.order.Address;

public class OrderCancelAndRejectPayload extends OrderEventPayload {

    private String containerID;
    private String voyageID;
    private String reason;

    public OrderCancelAndRejectPayload(String orderID, String productID, String customerID, String containerID, String voyageID, int quantity, Address pickupAddress,
            String pickupDate, Address destinationAddress, String expectedDeliveryDate, String status, String reason) {
        super(orderID,productID,customerID,quantity,pickupAddress,pickupDate,destinationAddress,expectedDeliveryDate,status);
        this.containerID = containerID;
        this.voyageID = voyageID;
        this.reason = reason;
    }

	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
    }
    
    public String getVoyageID() {
		return voyageID;
	}

	public void setVoyageID(String voyageID) {
		this.voyageID = voyageID;
    }
    
    public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}