package ibm.gse.orderms.infrastructure.events.reefer;

import ibm.gse.orderms.infrastructure.events.OrderEventBase;

public class ReeferAssignedEvent extends OrderEventBase {

	private ReeferAssignment payload;

	public ReeferAssignedEvent(long timestampMillis, String version,ReeferAssignment payload) {
		this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEventBase.TYPE_REEFER_ASSIGNED;
    	this.payload = payload;
	}

	public ReeferAssignedEvent() {
		
	}
	
	
	public ReeferAssignment getPayload() {
		return payload;
	}

	public void setPayload(ReeferAssignment payload) {
		this.payload = payload;
	}
	
	
}
