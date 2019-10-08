package ibm.gse.orderms.infrastructure.events.voyage;

import ibm.gse.orderms.infrastructure.events.OrderEventBase;

public class VoyageAssignedEvent extends OrderEventBase {

  
	VoyageAssignment payload;
	
    public VoyageAssignedEvent(long timestampMillis, String version, VoyageAssignment payload) {
    	this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEventBase.TYPE_VOYAGE_ASSIGNED;
    	this.payload = payload;
    }

    public VoyageAssignedEvent() {
    }

	public VoyageAssignment getPayload() {
		return payload;
	}

    

}
