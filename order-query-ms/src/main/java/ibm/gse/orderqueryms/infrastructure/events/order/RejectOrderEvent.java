package ibm.gse.orderqueryms.infrastructure.events.order;

import ibm.gse.orderqueryms.domain.model.Rejection;

public class RejectOrderEvent extends OrderEvent {
	
	private Rejection payload;
	
	public RejectOrderEvent(long timestampMillis, String version, Rejection payload) {
        super(timestampMillis, OrderEvent.TYPE_REJECTED, version);
        this.setPayload(payload);
    }

    public RejectOrderEvent() {
    }

    @Override
    public Rejection getPayload() {
        return payload;
    }

    public void setPayload(Rejection payload) {
        this.payload = payload;
    }

}
