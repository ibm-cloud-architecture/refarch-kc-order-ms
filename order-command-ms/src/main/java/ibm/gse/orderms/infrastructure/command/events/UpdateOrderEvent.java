package ibm.gse.orderms.infrastructure.command.events;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.events.OrderEvent;

public class UpdateOrderEvent extends OrderEvent {

    private ShippingOrder payload;

    public UpdateOrderEvent(long timestampMillis, String version, ShippingOrder payload) {
        super(timestampMillis, OrderEvent.TYPE_UPDATED, version);
        this.payload = payload;
    }

    public UpdateOrderEvent() {
    }

    public ShippingOrder getPayload() {
        return payload;
    }

    public void setPayload(ShippingOrder payload) {
        this.payload = payload;
    }

}
