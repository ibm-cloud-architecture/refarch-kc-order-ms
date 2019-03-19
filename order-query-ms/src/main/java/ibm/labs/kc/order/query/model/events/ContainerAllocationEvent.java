package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.ContainerAssignment;

public class ContainerAllocationEvent extends ContainerEvent {
	
	private ContainerAssignment payload;

	public ContainerAllocationEvent(long timestampMillis, String version,ContainerAssignment payload) {
		super(timestampMillis,ContainerEvent.TYPE_ASSIGNED,version);
		this.payload = payload;
	}

	public ContainerAllocationEvent() {
		
	}
	
	public ContainerAssignment getPayload() {
		return payload;
	}

	public void setPayload(ContainerAssignment payload) {
		this.payload = payload;
	}

}
