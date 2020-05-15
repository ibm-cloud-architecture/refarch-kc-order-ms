package ibm.gse.orderms.infrastructure.events.voyage;

import ibm.gse.orderms.infrastructure.events.EventBase;

public class VoyageAssignedEvent extends EventBase {

  
	VoyageAssignmentPayload payload;
	
    public VoyageAssignedEvent(long timestampMillis, String version, VoyageAssignmentPayload payload) {
    	this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = EventBase.TYPE_VOYAGE_ASSIGNED;
    	this.payload = payload;
    }

    public VoyageAssignedEvent() {
    }

	public VoyageAssignmentPayload getPayload() {
		return payload;
	}

    

}
