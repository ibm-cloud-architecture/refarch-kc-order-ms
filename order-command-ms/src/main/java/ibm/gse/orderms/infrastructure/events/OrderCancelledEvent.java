package ibm.gse.orderms.infrastructure.events;

public class OrderCancelledEvent extends OrderEventBase {

    private OrderCancelAndRejectPayload payload;

    public OrderCancelledEvent(long timestampMillis, String version, OrderCancelAndRejectPayload payload) {
        this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEventBase.TYPE_ORDER_CANCELLED;
    	this.payload = payload;
        this.setPayload(payload);
    }

    public OrderCancelledEvent() {
    }

    
    public OrderCancelAndRejectPayload getPayload() {
        return payload;
    }

    public void setPayload(OrderCancelAndRejectPayload payload) {
        this.payload = payload;
    }

}
