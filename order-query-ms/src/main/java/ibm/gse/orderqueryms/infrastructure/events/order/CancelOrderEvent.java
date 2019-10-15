package ibm.gse.orderqueryms.infrastructure.events.order;

import ibm.gse.orderqueryms.domain.model.Cancellation;

public class CancelOrderEvent extends OrderEvent {

    private Cancellation payload;

    public CancelOrderEvent(long timestampMillis, String version, Cancellation payload) {
        super(timestampMillis, OrderEvent.TYPE_CANCELLED, version);
        this.setPayload(payload);
    }

    public CancelOrderEvent() {
    }

    @Override
    public Cancellation getPayload() {
        return payload;
    }

    public void setPayload(Cancellation payload) {
        this.payload = payload;
    }

}
