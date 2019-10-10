package ibm.gse.orderms.infrastructure.events.voyage;

import ibm.gse.orderms.infrastructure.events.OrderEventBase;

public class VoyageAssignedEvent extends OrderEventBase {

  
	VoyageAssignmentPayload payload;
	
    public VoyageAssignedEvent(long timestampMillis, String version, VoyageAssignmentPayload payload) {
    	this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEventBase.TYPE_VOYAGE_ASSIGNED;
    	this.payload = payload;
    }

    public VoyageAssignedEvent() {
    }

	public VoyageAssignmentPayload getPayload() {
		return payload;
	}

    

}
