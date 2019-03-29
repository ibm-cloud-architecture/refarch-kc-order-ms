package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerOffTruckEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerOffTruckEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_OFF_TRUCK, version);
        this.payload = payload;
    }

    public ContainerOffTruckEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
