package ibm.gse.orderqueryms.infrastructure.events.container;

import ibm.gse.orderqueryms.domain.model.Container;

public class ContainerOnMaintainanceEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerOnMaintainanceEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_ON_MAINTENANCE, version);
        this.payload = payload;
    }

    public ContainerOnMaintainanceEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
