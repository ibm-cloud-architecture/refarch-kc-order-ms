package ibm.gse.orderms.infrastructure.events;

public class OrderRejectEvent extends OrderEventBase {

    private OrderCancelAndRejectPayload payload;

    public OrderRejectEvent(long timestampMillis, String version, OrderCancelAndRejectPayload payload) {
        super(timestampMillis, OrderEventBase.TYPE_ORDER_REJECTED, version);
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
