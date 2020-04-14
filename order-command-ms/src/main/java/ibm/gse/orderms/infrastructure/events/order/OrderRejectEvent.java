package ibm.gse.orderms.infrastructure.events.order;

import ibm.gse.orderms.infrastructure.events.EventBase;

public class OrderRejectEvent extends EventBase {

    private OrderCancelAndRejectPayload payload;

    public OrderRejectEvent(long timestampMillis, String version, OrderCancelAndRejectPayload payload) {
        super(timestampMillis, EventBase.TYPE_ORDER_REJECTED, version);
    	this.payload = payload;
    }

    public OrderRejectEvent() {
    }

    
    public OrderCancelAndRejectPayload getPayload() {
        return payload;
    }

    public void setPayload(OrderCancelAndRejectPayload payload) {
        this.payload = payload;
    }

}
