package ibm.gse.orderms.infrastructure.command.events;

import com.google.gson.Gson;

import ibm.gse.orderms.infrastructure.events.EventBase;
import ibm.gse.orderms.infrastructure.events.order.OrderEventPayload;

public class OrderCommandEvent extends EventBase {

    public static final String TYPE_CREATE_ORDER = "CreateOrderCommand";
	public static final String TYPE_UPDATE_ORDER = "UpdateOrderCommand";
	public static final String TYPE_CANCEL_ORDER = "CancelOrderCommand";
    private static final Gson gson = new Gson();
    private OrderEventPayload payload;
	
	public OrderCommandEvent(long currentTimeMillis, String version, OrderEventPayload order, String orderEventType) {
		this.timestampMillis = currentTimeMillis;
		this.version = version;
		this.payload = order;
		this.type = orderEventType;
	}


	public static OrderCommandEvent deserialize(String json) {
        return gson.fromJson(json, OrderCommandEvent.class);
	}
	
	
	public OrderEventPayload getPayload() {
		return payload;
	}

}
