package ibm.gse.orderms.infrastructure.events;

import ibm.gse.orderms.domain.model.order.ShippingOrder;

public class OrderCreatedEvent extends OrderEvent {

    private ShippingOrder payload;

    public OrderCreatedEvent(long timestampMillis, String version, ShippingOrder payload) {
        super(timestampMillis, OrderEvent.TYPE_CREATED, version);
        this.payload = payload;
    }

    public OrderCreatedEvent() {
    }

    public ShippingOrder getPayload() {
        return payload;
    }

    public void setPayload(ShippingOrder payload) {
        this.payload = payload;
    }

}
