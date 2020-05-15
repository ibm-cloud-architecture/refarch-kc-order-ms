package ibm.gse.orderms.infrastructure.events.order;

import ibm.gse.orderms.infrastructure.events.EventBase;

public class OrderSpoiltEvent extends EventBase {

    private String orderID;
    private OrderSpoiltPayload payload;

    public OrderSpoiltEvent(long timestampMillis, String version, String orderID, OrderSpoiltPayload payload) {
        this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = EventBase.TYPE_ORDER_SPOILT;
    	this.payload = payload;
        this.setPayload(payload);
        this.orderID = orderID;
    }

    public OrderSpoiltEvent() {
    }

    
    public OrderSpoiltPayload getPayload() {
        return payload;
    }

    public void setPayload(OrderSpoiltPayload payload) {
        this.payload = payload;
    }

    public String getOrderId() {
        return orderID;
    }

}
