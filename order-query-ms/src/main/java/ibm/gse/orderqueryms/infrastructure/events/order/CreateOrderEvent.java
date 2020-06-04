package ibm.gse.orderqueryms.infrastructure.events.order;

import ibm.gse.orderqueryms.domain.model.Order;

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
