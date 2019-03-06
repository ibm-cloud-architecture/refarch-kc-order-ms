package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.ContainerAssignment;

public class ContainerOffShipEvent extends OrderEvent {
	
    private ContainerAssignment payload;
	
	public ContainerOffShipEvent(long timestampMillis, String version, ContainerAssignment payload) {
        super(timestampMillis, OrderEvent.TYPE_CONTAINER_OFF_SHIP, version);
        this.setPayload(payload);
    }

    public ContainerOffShipEvent() {
    }

    @Override
    public ContainerAssignment getPayload() {
        return payload;
    }

    public void setPayload(ContainerAssignment payload) {
        this.payload = payload;
    }

}
