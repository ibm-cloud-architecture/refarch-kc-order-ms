package ibm.gse.orderms.domain.events.order;

import ibm.gse.orderms.domain.events.EventBase;

public class OrderRejectEvent extends EventBase {

    private OrderCancelAndRejectPayload payload;

    public OrderRejectEvent(long timestampMillis, String version, OrderCancelAndRejectPayload payload) {
        super(timestampMillis, EventBase.ORDER_REJECTED_TYPE, version);
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
