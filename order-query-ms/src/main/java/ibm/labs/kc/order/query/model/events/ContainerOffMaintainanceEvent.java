package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerOffMaintainanceEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerOffMaintainanceEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_OFF_MAINTENANCE, version);
        this.payload = payload;
    }

    public ContainerOffMaintainanceEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
