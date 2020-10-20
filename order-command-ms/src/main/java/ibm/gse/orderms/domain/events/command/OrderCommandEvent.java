package ibm.gse.orderms.domain.events.command;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.events.EventBase;
import ibm.gse.orderms.domain.events.order.OrderEventPayload;

public class OrderCommandEvent extends EventBase {

    public static final String ORDER_CREATED_TYPE = "CreateOrderCommand";
	public static final String UPDATED_ORDER_TYPE = "UpdateOrderCommand";
	public static final String CANCELLED_ORDER_TYPE = "CancelOrderCommand";
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
