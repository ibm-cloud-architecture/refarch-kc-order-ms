package ibm.gse.orderms.infrastructure.command.events;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.events.OrderEventBase;

public class OrderCommandEvent extends OrderEventBase {

    public static final String TYPE_CREATE_ORDER = "CreateOrderCommand";
    public static final String TYPE_UPDATE_ORDER = "UpdateOrderCommand";
    private static final Gson gson = new Gson();
    private ShippingOrder payload;
	
	public OrderCommandEvent(long currentTimeMillis, String version, ShippingOrder order, String orderEventType) {
		this.timestampMillis = currentTimeMillis;
		this.version = version;
		this.payload = order;
		this.type = orderEventType;
	}


	public static OrderCommandEvent deserialize(String json) {
        return gson.fromJson(json, OrderCommandEvent.class);
	}
	
	
	public ShippingOrder getPayload() {
		return payload;
	}

}
