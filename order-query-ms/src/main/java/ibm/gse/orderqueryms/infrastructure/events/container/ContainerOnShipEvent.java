package ibm.gse.orderqueryms.infrastructure.events.container;

import ibm.gse.orderqueryms.domain.model.Container;

public class ContainerOnShipEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerOnShipEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_ON_SHIP, version);
        this.payload = payload;
    }

    public ContainerOnShipEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
