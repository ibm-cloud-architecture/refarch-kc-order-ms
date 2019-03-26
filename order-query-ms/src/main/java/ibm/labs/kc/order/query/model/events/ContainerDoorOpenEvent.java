package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerDoorOpenEvent extends ContainerEvent{
	
    private Container payload;
	
	public ContainerDoorOpenEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_DOOR_OPEN, version);
        this.payload = payload;
    }

    public ContainerDoorOpenEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
