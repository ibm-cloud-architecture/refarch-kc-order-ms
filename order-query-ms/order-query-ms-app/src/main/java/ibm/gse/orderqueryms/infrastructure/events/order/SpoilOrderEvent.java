package ibm.gse.orderqueryms.infrastructure.events.order;

import ibm.gse.orderqueryms.domain.model.Spoil;

public class SpoilOrderEvent extends OrderEvent {

    private String orderID;
    private Spoil payload;

    public SpoilOrderEvent(long timestampMillis, String version, String orderID, Spoil payload) {
        this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEvent.TYPE_SPOILT;
    	this.payload = payload;
        this.setPayload(payload);
        this.orderID = orderID;
    }

    public SpoilOrderEvent() {
    }

    
    public Spoil getPayload() {
        return payload;
    }

    public void setPayload(Spoil payload) {
        this.payload = payload;
    }

    public String getOrderId() {
        return orderID;
    }

}
