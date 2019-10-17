package ibm.gse.orderqueryms.infrastructure.events.container;

import ibm.gse.orderqueryms.domain.model.Container;

public class ContainerGoodsUnLoadedEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerGoodsUnLoadedEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_CONTAINER_GOOD_UNLOADED, version);
        this.payload = payload;
    }

    public ContainerGoodsUnLoadedEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}


}
