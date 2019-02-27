package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class AllocatedContainerEvent extends OrderEvent {
	
	private Container payload;
	
	public AllocatedContainerEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, OrderEvent.TYPE_CONTAINER_ALLOCATED_STATUS, version);
        this.setPayload(payload);
    }

    public AllocatedContainerEvent() {
    }

    @Override
    public Container getPayload() {
        return payload;
    }

    public void setPayload(Container payload) {
        this.payload = payload;
    }

}
