package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerOnTruckEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerOnTruckEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_ON_TRUCK, version);
        this.payload = payload;
    }

    public ContainerOnTruckEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
