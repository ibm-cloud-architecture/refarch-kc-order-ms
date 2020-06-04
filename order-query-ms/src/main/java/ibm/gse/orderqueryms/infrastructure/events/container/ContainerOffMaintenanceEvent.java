package ibm.gse.orderqueryms.infrastructure.events.container;

import ibm.gse.orderqueryms.domain.model.Container;

public class ContainerOffMaintenanceEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerOffMaintenanceEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_OFF_MAINTENANCE, version);
        this.payload = payload;
    }

    public ContainerOffMaintenanceEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
