package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.ContainerAssignment;

public class ContainerOnShipEvent extends OrderEvent{
	
    private ContainerAssignment payload;
	
	public ContainerOnShipEvent(long timestampMillis, String version, ContainerAssignment payload) {
        super(timestampMillis, OrderEvent.TYPE_CONTAINER_ON_SHIP, version);
        this.setPayload(payload);
    }

    public ContainerOnShipEvent() {
    }

    @Override
    public ContainerAssignment getPayload() {
        return payload;
    }

    public void setPayload(ContainerAssignment payload) {
        this.payload = payload;
    }

}
