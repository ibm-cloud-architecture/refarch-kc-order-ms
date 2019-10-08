package ibm.gse.orderms.infrastructure.events;

public class OrderCancelledEvent extends OrderEventBase {

    private CancellationPayload payload;

    public OrderCancelledEvent(long timestampMillis, String version, CancellationPayload payload) {
        this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEventBase.TYPE_ORDER_CANCELLED;
    	this.payload = payload;
        this.setPayload(payload);
    }

    public OrderCancelledEvent() {
    }

    
    public CancellationPayload getPayload() {
        return payload;
    }

    public void setPayload(CancellationPayload payload) {
        this.payload = payload;
    }

}
