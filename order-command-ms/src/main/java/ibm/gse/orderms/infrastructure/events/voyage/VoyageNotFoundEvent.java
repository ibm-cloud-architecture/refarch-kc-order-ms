package ibm.gse.orderms.infrastructure.events.voyage;

import ibm.gse.orderms.infrastructure.events.OrderEventBase;

public class VoyageNotFoundEvent extends OrderEventBase {

  
	VoyageNotFoundPayload payload;
	
    public VoyageNotFoundEvent(long timestampMillis, String version, VoyageNotFoundPayload payload) {
    	this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEventBase.TYPE_VOYAGE_NOT_FOUND;
    	this.payload = payload;
    }

    public VoyageNotFoundEvent() {
    }

	public VoyageNotFoundPayload getPayload() {
		return payload;
	}

    

}
