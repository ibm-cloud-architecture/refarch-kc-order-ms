package ibm.gse.orderqueryms.infrastructure.events.order;

import ibm.gse.orderqueryms.domain.model.CancelAndRejectPayload;

public class CancelOrderEvent extends OrderEvent {

    private CancelAndRejectPayload payload;

    public CancelOrderEvent(long timestampMillis, String version, CancelAndRejectPayload payload) {
        super(timestampMillis, OrderEvent.TYPE_CANCELLED, version);
        this.setPayload(payload);
    }

    public CancelOrderEvent() {
    }

    @Override
    public CancelAndRejectPayload getPayload() {
        return payload;
    }

    public void setPayload(CancelAndRejectPayload payload) {
        this.payload = payload;
    }

}
