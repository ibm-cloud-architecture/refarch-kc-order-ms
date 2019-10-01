package ibm.gse.orderms.infrastructure.command.events;

import ibm.gse.orderms.infrastructure.events.OrderEvent;
import ibm.gse.orderms.infrastructure.events.VoyageAssignment;

public class AssignOrderEvent extends OrderEvent {

    private VoyageAssignment payload;

    public AssignOrderEvent(long timestampMillis, String version, VoyageAssignment payload) {
        super(timestampMillis, OrderEvent.TYPE_ASSIGNED, version);
        this.setPayload(payload);
    }

    public AssignOrderEvent() {
    }

    @Override
    public VoyageAssignment getPayload() {
        return payload;
    }

    public void setPayload(VoyageAssignment payload) {
        this.payload = payload;
    }

}
