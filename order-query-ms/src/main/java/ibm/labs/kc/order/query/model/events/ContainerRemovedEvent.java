package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerRemovedEvent extends ContainerEvent {
	
	private Container payload;
	
	public ContainerRemovedEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_REMOVED, version);
        this.payload = payload;
    }

    public ContainerRemovedEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
