package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerDeliveredEvent extends OrderEvent {
	
    private Container payload;
	
	public ContainerDeliveredEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, OrderEvent.TYPE_CONTAINER_DELIVERED_STATUS, version);
        this.setPayload(payload);
    }

    public ContainerDeliveredEvent() {
    }

    @Override
    public Container getPayload() {
        return payload;
    }

    public void setPayload(Container payload) {
        this.payload = payload;
    }

}
