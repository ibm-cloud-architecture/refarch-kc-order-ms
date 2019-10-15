package ibm.gse.orderqueryms.infrastructure.events.order;

import ibm.gse.orderqueryms.domain.model.VoyageAssignment;

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
