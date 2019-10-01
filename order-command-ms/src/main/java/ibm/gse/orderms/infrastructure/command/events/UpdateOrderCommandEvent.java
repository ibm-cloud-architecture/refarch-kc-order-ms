package ibm.gse.orderms.infrastructure.command.events;

import ibm.gse.orderms.domain.model.order.ShippingOrder;

public class UpdateOrderCommandEvent extends OrderCommandEvent {

	protected ShippingOrder payload;
	
	
	public UpdateOrderCommandEvent(long timestampMillis, String version, ShippingOrder payload) {
		this.payload = payload;
		this.type = OrderCommandEvent.TYPE_UPDATE_ORDER;
	}
	
	public ShippingOrder getPayload() {
		return this.payload;
	};
	

}
