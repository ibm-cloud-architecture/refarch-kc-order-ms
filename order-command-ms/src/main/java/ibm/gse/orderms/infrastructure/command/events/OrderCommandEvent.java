package ibm.gse.orderms.infrastructure.command.events;

import com.google.gson.Gson;

import ibm.gse.orderms.infrastructure.events.AbstractEvent;

public class OrderCommandEvent extends AbstractEvent {

    public static final String TYPE_CREATE_ORDER = "CreateOrderCommand";
    public static final String TYPE_UPDATE_ORDER = "UpdateOrderCommand";
    private static final Gson gson = new Gson();
    
	@Override
	public Object getPayload() {
		// TODO Auto-generated method stub
		return null;
	}

	public static OrderCommandEvent deserialize(String json) {
        // OrderEvent is a concrete class just to find the type of the event
        // We could do a "normal" JSON deserialization instead
        OrderCommandEvent orderEvent = gson.fromJson(json, OrderCommandEvent.class);
        switch (orderEvent.type) {
        case TYPE_CREATE_ORDER:
            return gson.fromJson(json, CreateOrderCommandEvent.class);
        case TYPE_UPDATE_ORDER:
            return gson.fromJson(json, UpdateOrderCommandEvent.class);
        default:
            //TODO handle
            return null;
        }
	}

}
