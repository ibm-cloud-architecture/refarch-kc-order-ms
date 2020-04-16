package ibm.gse.orderms.infrastructure.events.container;

import ibm.gse.orderms.infrastructure.events.EventBase;

public class ContainerNotFoundEvent extends EventBase {

	private ContainerNotFoundPayload payload;

	public ContainerNotFoundEvent(long timestampMillis, String version, ContainerNotFoundPayload payload) {
		this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = EventBase.TYPE_CONTAINER_NOT_FOUND;
    	this.payload = payload;
	}

	public ContainerNotFoundEvent() {
		
	}
	
	
	public ContainerNotFoundPayload getPayload() {
		return payload;
	}

	public void setPayload(ContainerNotFoundPayload payload) {
		this.payload = payload;
	}
	
	
}
