package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class AvailableContainerEvent extends ContainerEvent {
	
	private Container payload;
	
	public AvailableContainerEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_AVAILABLE, version);
        this.payload = payload;
    }

    public AvailableContainerEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
