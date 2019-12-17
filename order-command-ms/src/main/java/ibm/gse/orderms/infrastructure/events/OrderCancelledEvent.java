package ibm.gse.orderms.infrastructure.events;

public class OrderCancelledEvent extends OrderEventBase {

    private OrderCancellationPayload payload;

    public OrderCancelledEvent(long timestampMillis, String version, OrderCancellationPayload payload) {
        this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEventBase.TYPE_ORDER_CANCELLED;
    	this.payload = payload;
        this.setPayload(payload);
    }

    public OrderCancelledEvent() {
    }

    
    public OrderCancellationPayload getPayload() {
        return payload;
    }

    public void setPayload(OrderCancellationPayload payload) {
        this.payload = payload;
    }

}
