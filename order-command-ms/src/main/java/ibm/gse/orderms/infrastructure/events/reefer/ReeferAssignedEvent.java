package ibm.gse.orderms.infrastructure.events.reefer;

import ibm.gse.orderms.infrastructure.events.OrderEventBase;

public class ReeferAssignedEvent extends OrderEventBase {

	private ReeferAssignmentPayload payload;

	public ReeferAssignedEvent(long timestampMillis, String version,ReeferAssignmentPayload payload) {
		this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEventBase.TYPE_CONTAINER_ALLOCATED;
    	this.payload = payload;
	}

	public ReeferAssignedEvent() {
		
	}
	
	
	public ReeferAssignmentPayload getPayload() {
		return payload;
	}

	public void setPayload(ReeferAssignmentPayload payload) {
		this.payload = payload;
	}
	
	
}
