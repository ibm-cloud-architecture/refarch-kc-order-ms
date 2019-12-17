package ibm.gse.orderms.infrastructure.events;

public class OrderRejectEvent extends OrderEventBase {

    private OrderRejectPayload payload;

    public OrderRejectEvent(long timestampMillis, String version, OrderRejectPayload payload) {
        super(timestampMillis, OrderEventBase.TYPE_ORDER_REJECTED, version);
    	this.payload = payload;
    }

    public OrderRejectEvent() {
    }

    
    public OrderRejectPayload getPayload() {
        return payload;
    }

    public void setPayload(OrderRejectPayload payload) {
        this.payload = payload;
    }

}
