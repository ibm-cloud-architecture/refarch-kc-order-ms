package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerDoorClosedEvent extends ContainerEvent{
	
    private Container payload;
	
	public ContainerDoorClosedEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_DOOR_CLOSED, version);
        this.payload = payload;
    }

    public ContainerDoorClosedEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
