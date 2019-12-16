package ibm.gse.orderms.infrastructure.events.reefer;

import ibm.gse.orderms.infrastructure.events.OrderEventBase;

public class ReeferNotFoundEvent extends OrderEventBase {

	private ReeferNotFoundPayload payload;

	public ReeferNotFoundEvent(long timestampMillis, String version, ReeferNotFoundPayload payload) {
		this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEventBase.TYPE_CONTAINER_NOT_FOUND;
    	this.payload = payload;
	}

	public ReeferNotFoundEvent() {
		
	}
	
	
	public ReeferNotFoundPayload getPayload() {
		return payload;
	}

	public void setPayload(ReeferNotFoundPayload payload) {
		this.payload = payload;
	}
	
	
}
