package ibm.labs.kc.order.command.model.events;

import ibm.labs.kc.order.command.model.Order;

public class OrderEvent extends AbstractEvent {

    public static final String TYPE_CREATED = "OrderCreated";
    public static final String TYPE_UPDATED = "OrderUpdated";
    public static final String TYPE_ASSIGNED = "OrderAssigned";
    public static final String TYPE_CANCELLED = "OrderCancelled";

    private Order payload;

    public OrderEvent(long timestampMillis, String type, String version, Order payload) {
        super(timestampMillis, type, version);
        this.payload = payload;
    }

    public OrderEvent() {
    }

    public Order getPayload() {
        return payload;
    }

    public void setPayload(Order payload) {
        this.payload = payload;
    }
}
