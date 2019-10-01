package ibm.gse.orderms.infrastructure.command.events;

import ibm.gse.orderms.domain.model.order.ShippingOrder;

public class CreateOrderCommandEvent extends OrderCommandEvent {

	protected ShippingOrder payload;
	
	public CreateOrderCommandEvent(long timestampMillis, String version, ShippingOrder payload) {
		this.payload = payload;
		this.type = OrderCommandEvent.TYPE_CREATE_ORDER;
	}
	
	@Override
	public ShippingOrder getPayload() {
        return this.payload;
    }
}
