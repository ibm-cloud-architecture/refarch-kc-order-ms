package ibm.gse.orderqueryms.infrastructure.events.container;

import ibm.gse.orderqueryms.domain.model.Container;

public class ContainerAtLocationEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerAtLocationEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_AT_LOCATION, version);
        this.payload = payload;
    }

    public ContainerAtLocationEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
