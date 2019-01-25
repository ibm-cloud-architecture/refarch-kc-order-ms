package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Order;

public class CreateOrderEvent extends OrderEvent {

    private Order payload;

    public CreateOrderEvent(long timestampMillis, String version, Order payload) {
        super(timestampMillis, OrderEvent.TYPE_CREATED, version);
        this.payload = payload;
    }

    public CreateOrderEvent() {
    }

    public Order getPayload() {
        return payload;
    }

    public void setPayload(Order payload) {
        this.payload = payload;
    }

}
