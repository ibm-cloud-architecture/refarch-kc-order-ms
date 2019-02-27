package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerOffShipEvent extends OrderEvent {
	
    private Container payload;
	
	public ContainerOffShipEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, OrderEvent.TYPE_CONTAINER_OFF_SHIP_STATUS, version);
        this.setPayload(payload);
    }

    public ContainerOffShipEvent() {
    }

    @Override
    public Container getPayload() {
        return payload;
    }

    public void setPayload(Container payload) {
        this.payload = payload;
    }

}
