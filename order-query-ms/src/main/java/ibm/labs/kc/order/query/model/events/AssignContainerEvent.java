package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.ContainerAssignment;

public class AssignContainerEvent extends OrderEvent {

	private ContainerAssignment payload;

	public AssignContainerEvent(long timestampMillis, String version,ContainerAssignment payload) {
		super(timestampMillis,OrderEvent.TYPE_CONTAINER_ALLOCATED,version);
		this.payload = payload;
	}

	public AssignContainerEvent() {
		
	}
	
	
	public ContainerAssignment getPayload() {
		return payload;
	}

	public void setPayload(ContainerAssignment payload) {
		this.payload = payload;
	}
	
	
}
