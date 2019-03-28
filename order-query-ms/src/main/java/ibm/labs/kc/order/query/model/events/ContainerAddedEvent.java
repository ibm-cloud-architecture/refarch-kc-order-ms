package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerAddedEvent extends ContainerEvent {
	
	private Container payload;
	
	public ContainerAddedEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_ADDED, version);
        this.payload = payload;
    }

    public ContainerAddedEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
