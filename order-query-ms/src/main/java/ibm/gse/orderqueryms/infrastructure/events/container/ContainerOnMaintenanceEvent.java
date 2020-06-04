package ibm.gse.orderqueryms.infrastructure.events.container;

import ibm.gse.orderqueryms.domain.model.Container;

public class ContainerOnMaintenanceEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerOnMaintenanceEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_ON_MAINTENANCE, version);
        this.payload = payload;
    }

    public ContainerOnMaintenanceEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
