package ibm.gse.orderms.infrastructure.command.events;

import ibm.gse.orderms.infrastructure.events.OrderEvent;
import ibm.gse.orderms.infrastructure.events.VoyageAssignment;

public class OrderAssignedEvent extends OrderEvent {

    private VoyageAssignment payload;

    public OrderAssignedEvent(long timestampMillis, String version, VoyageAssignment payload) {
        super(timestampMillis, OrderEvent.TYPE_ASSIGNED, version);
        this.setPayload(payload);
    }

    public OrderAssignedEvent() {
    }

    @Override
    public VoyageAssignment getPayload() {
        return payload;
    }

    public void setPayload(VoyageAssignment payload) {
        this.payload = payload;
    }

}
