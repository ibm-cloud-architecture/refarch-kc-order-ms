package ibm.gse.orderms.infrastructure.events.container;

import ibm.gse.orderms.infrastructure.events.EventBase;

public class ContainerAllocatedEvent extends EventBase {

	private String orderID;
	private ContainerAllocatedPayload payload;

	public ContainerAllocatedEvent(long timestampMillis, String version, String orderID, ContainerAllocatedPayload payload) {
		this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = EventBase.TYPE_CONTAINER_ALLOCATED;
		this.payload = payload;
		this.orderID = orderID;
	}

	public ContainerAllocatedEvent() {
		
	}
	
	public ContainerAllocatedPayload getPayload() {
		return payload;
	}

	public void setPayload(ContainerAllocatedPayload payload) {
		this.payload = payload;
	}

	public String getOrderId() {
		return orderID;
	}
	
}
