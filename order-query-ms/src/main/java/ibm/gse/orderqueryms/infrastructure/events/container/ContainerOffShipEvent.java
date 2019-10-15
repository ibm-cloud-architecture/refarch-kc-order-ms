package ibm.gse.orderqueryms.infrastructure.events.container;

import ibm.gse.orderqueryms.domain.model.Container;

public class ContainerOffShipEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerOffShipEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_OFF_SHIP, version);
        this.payload = payload;
    }

    public ContainerOffShipEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
