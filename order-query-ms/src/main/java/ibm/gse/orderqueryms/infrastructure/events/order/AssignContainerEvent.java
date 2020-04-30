package ibm.gse.orderqueryms.infrastructure.events.order;

import ibm.gse.orderqueryms.domain.model.ContainerAssignment;

public class AssignContainerEvent extends OrderEvent{

	private String orderID;
	private ContainerAssignment payload;

	public AssignContainerEvent(long timestampMillis, String version, String orderID, ContainerAssignment payload) {
		super(timestampMillis,OrderEvent.TYPE_CONTAINER_ALLOCATED,version);
		this.payload = payload;
		this.orderID = orderID;
	}

	public AssignContainerEvent() {
		
	}
	
	public ContainerAssignment getPayload() {
		return payload;
	}

	public void setPayload(ContainerAssignment payload) {
		this.payload = payload;
	}

	public String getOrderId() {
		return orderID;
	}
	
	
}
