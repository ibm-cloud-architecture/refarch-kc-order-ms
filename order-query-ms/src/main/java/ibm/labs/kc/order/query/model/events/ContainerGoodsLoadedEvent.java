package ibm.labs.kc.order.query.model.events;

import ibm.labs.kc.order.query.model.Container;

public class ContainerGoodsLoadedEvent extends ContainerEvent {
	
    private Container payload;
	
	public ContainerGoodsLoadedEvent(long timestampMillis, String version, Container payload) {
        super(timestampMillis, ContainerEvent.TYPE_GOODS_LOADED, version);
        this.payload = payload;
    }

    public ContainerGoodsLoadedEvent() {
    }

	public Container getPayload() {
		return payload;
	}

	public void setPayload(Container payload) {
		this.payload = payload;
	}

}
