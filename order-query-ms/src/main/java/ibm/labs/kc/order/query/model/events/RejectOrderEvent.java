package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Rejection;

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
