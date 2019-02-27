package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerOnShipEvent extends OrderEvent{
	
    private Container payload;
	
	public ContainerOnShipEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, OrderEvent.TYPE_CONTAINER_ON_SHIP_STATUS, version);
        this.setPayload(payload);
    }

    public ContainerOnShipEvent() {
    }

    @Override
    public Container getPayload() {
        return payload;
    }

    public void setPayload(Container payload) {
        this.payload = payload;
    }

}
