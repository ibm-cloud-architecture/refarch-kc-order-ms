package ibm.gse.orderms.infrastructure.events;

import ibm.gse.orderms.domain.model.order.ShippingOrder;

public class OrderUpdatedEvent extends OrderEvent {

    private ShippingOrder payload;

    public OrderUpdatedEvent(long timestampMillis, String version, ShippingOrder payload) {
        super(timestampMillis, OrderEvent.TYPE_UPDATED, version);
        this.payload = payload;
    }

    public OrderUpdatedEvent() {
    }

    public ShippingOrder getPayload() {
        return payload;
    }

    public void setPayload(ShippingOrder payload) {
        this.payload = payload;
    }

}
