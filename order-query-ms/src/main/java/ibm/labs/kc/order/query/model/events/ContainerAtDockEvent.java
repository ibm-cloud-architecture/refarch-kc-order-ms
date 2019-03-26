package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerAtDockEvent extends ContainerEvent{
	
    private Container payload;
	
	public ContainerAtDockEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_AT_DOCK, version);
        this.payload = payload;
    }

    public ContainerAtDockEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
