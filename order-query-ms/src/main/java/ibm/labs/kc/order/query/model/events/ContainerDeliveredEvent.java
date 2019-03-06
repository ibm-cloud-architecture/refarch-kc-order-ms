package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.ContainerAssignment;

public class ContainerDeliveredEvent extends OrderEvent {
	
    private ContainerAssignment payload;
	
	public ContainerDeliveredEvent(long timestampMillis, String version, ContainerAssignment payload) {
        super(timestampMillis, OrderEvent.TYPE_CONTAINER_DELIVERED, version);
        this.setPayload(payload);
    }

    public ContainerDeliveredEvent() {
    }

    @Override
    public ContainerAssignment getPayload() {
        return payload;
    }

    public void setPayload(ContainerAssignment payload) {
        this.payload = payload;
    }

}
