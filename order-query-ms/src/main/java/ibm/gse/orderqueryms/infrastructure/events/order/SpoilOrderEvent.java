package ibm.gse.orderqueryms.infrastructure.events.order;

import ibm.gse.orderqueryms.domain.model.Spoil;

public class SpoilOrderEvent extends OrderEvent {

    private Spoil payload;

    public SpoilOrderEvent(long timestampMillis, String version, Spoil payload) {
        this.timestampMillis = timestampMillis;
    	this.version = version;
    	this.type = OrderEvent.TYPE_SPOILT;
    	this.payload = payload;
        this.setPayload(payload);
    }

    public SpoilOrderEvent() {
    }

    
    public Spoil getPayload() {
        return payload;
    }

    public void setPayload(Spoil payload) {
        this.payload = payload;
    }

}
