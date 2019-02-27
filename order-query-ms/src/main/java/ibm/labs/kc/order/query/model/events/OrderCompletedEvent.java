package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Order;

public class OrderCompletedEvent extends OrderEvent {
	
    private Order payload;
	
	public OrderCompletedEvent(long timestampMillis, String version, Order payload) {
        super(timestampMillis, OrderEvent.TYPE_ORDER_COMPLETED, version);
        this.setPayload(payload);
    }

    public OrderCompletedEvent() {
    }

    @Override
    public Order getPayload() {
        return payload;
    }

    public void setPayload(Order payload) {
        this.payload = payload;
    }

}
