package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Order;

public class UpdateOrderEvent extends OrderEvent {

    private Order payload;

    public UpdateOrderEvent(long timestampMillis, String version, Order payload) {
        super(timestampMillis, OrderEvent.TYPE_UPDATED, version);
        this.payload = payload;
    }

    public UpdateOrderEvent() {
    }

    public Order getPayload() {
        return payload;
    }

    public void setPayload(Order payload) {
        this.payload = payload;
    }

}
