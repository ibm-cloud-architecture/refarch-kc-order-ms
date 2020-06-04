package ibm.gse.orderqueryms.infrastructure.events.order;

import ibm.gse.orderqueryms.domain.model.CancelAndRejectPayload;

public class RejectOrderEvent extends OrderEvent {
	
	private CancelAndRejectPayload payload;
	
	public RejectOrderEvent(long timestampMillis, String version, CancelAndRejectPayload payload) {
        super(timestampMillis, OrderEvent.TYPE_REJECTED, version);
        this.setPayload(payload);
    }

    public RejectOrderEvent() {
    }

    @Override
    public CancelAndRejectPayload getPayload() {
        return payload;
    }

    public void setPayload(CancelAndRejectPayload payload) {
        this.payload = payload;
    }

}
