package ibm.gse.orderms.infrastructure.command.events;

import ibm.gse.orderms.infrastructure.events.OrderEvent;
import ibm.labs.kc.order.command.model.Cancellation;

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
