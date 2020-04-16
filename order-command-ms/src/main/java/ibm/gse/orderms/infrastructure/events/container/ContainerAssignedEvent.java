package ibm.gse.orderms.infrastructure.events.container;

import ibm.gse.orderms.infrastructure.events.EventBase;

public class ContainerAssignedEvent extends EventBase {

	private ContainerAssignmentPayload payload;

	public ContainerAssignedEvent(long timestampMillis, String version,ContainerAssignmentPayload payload) {
		this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = EventBase.TYPE_CONTAINER_ALLOCATED;
    	this.payload = payload;
	}

	public ContainerAssignedEvent() {
		
	}
	
	
	public ContainerAssignmentPayload getPayload() {
		return payload;
	}

	public void setPayload(ContainerAssignmentPayload payload) {
		this.payload = payload;
	}
	
	
}
