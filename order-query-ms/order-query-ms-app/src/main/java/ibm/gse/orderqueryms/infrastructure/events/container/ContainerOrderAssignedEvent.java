package ibm.gse.orderqueryms.infrastructure.events.container;

import ibm.gse.orderqueryms.domain.model.Container;

public class ContainerOrderAssignedEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerOrderAssignedEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_ORDER_ASSIGNED, version);
        this.payload = payload;
    }

    public ContainerOrderAssignedEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
