package ibm.labs.kc.order.query.model.events;

import com.google.gson.Gson;

public class OrderEvent extends AbstractEvent {


    
    public static final String TYPE_CREATED = "OrderCreated";
    public static final String TYPE_UPDATED = "OrderUpdated";
    public static final String TYPE_BOOKED = "OrderBooked";
    public static final String TYPE_ASSIGNED = "OrderAssigned";
    public static final String TYPE_TRANSIT = "OrderInTransit";
    public static final String TYPE_COMPLETED = "OrderCompleted";
    public static final String TYPE_REJECTED = "OrderRejected";
    public static final String TYPE_CANCELLED = "OrderCancelled";
    
    public static final String TYPE_CONTAINER_ALLOCATED = "ContainerAllocated";
    public static final String TYPE_FULL_CONTAINER_VOYAGE_READY = "FullContainerVoyageReady";
    public static final String TYPE_CONTAINER_ON_SHIP = "ContainerOnShip";
    public static final String TYPE_CONTAINER_OFF_SHIP = "ContainerOffShip";
    public static final String TYPE_CONTAINER_DELIVERED = "ContainerDelivered";   

    private static final Gson gson = new Gson();

    public OrderEvent(long timestampMillis, String type, String version) {
        super(timestampMillis, type, version);
    }

    public OrderEvent() {}

    public static OrderEvent deserialize(String json) {
        // OrderEvent is a concrete class just to find the type of the event
        // We could do a "normal" JSON deserialization instead
        OrderEvent orderEvent = gson.fromJson(json, OrderEvent.class);
        switch (orderEvent.type) {
        case TYPE_CREATED:
            return gson.fromJson(json, CreateOrderEvent.class);
        case TYPE_ASSIGNED:
            return gson.fromJson(json, AssignOrderEvent.class);
        case TYPE_REJECTED:
        	return gson.fromJson(json, RejectOrderEvent.class);
        case TYPE_CONTAINER_ALLOCATED:
        	return gson.fromJson(json, AssignContainerEvent.class);
        case TYPE_CONTAINER_ON_SHIP:
        	return gson.fromJson(json, ContainerOnShipEvent.class);
        case TYPE_CONTAINER_OFF_SHIP:
        	return gson.fromJson(json, ContainerOffShipEvent.class);
        case TYPE_CONTAINER_DELIVERED:
        	return gson.fromJson(json, ContainerDeliveredEvent.class);
        case TYPE_UPDATED:
            return gson.fromJson(json, UpdateOrderEvent.class);
        case TYPE_CANCELLED:
            return gson.fromJson(json, CancelOrderEvent.class);
        case TYPE_COMPLETED:
        	return gson.fromJson(json, OrderCompletedEvent.class);
        default:
            //TODO handle
            return null;
        }
    }

    @Override
    public Object getPayload() {
        return null;
    }

}
