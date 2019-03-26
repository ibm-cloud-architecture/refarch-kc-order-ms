package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerAtPickUpSiteEvent extends ContainerEvent {
	
private Container payload;
	
	public ContainerAtPickUpSiteEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_PICK_UP_SITE, version);
        this.payload = payload;
    }

    public ContainerAtPickUpSiteEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
