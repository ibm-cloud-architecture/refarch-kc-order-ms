package ibm.labs.kc.order.query.model.events;

import com.google.gson.Gson;

public class ContainerEvent extends AbstractEvent {
	
	public static final String TYPE_CONTAINER_ADDED = "ContainerAdded";
	public static final String TYPE_CONTAINER_REMOVED = "ContainerRemoved";
	public static final String TYPE_CONTAINER_AT_LOCATION = "ContainerAtLocation";
	public static final String TYPE_CONTAINER_ON_MAINTENANCE = "ContainerOnMaintenance";
	public static final String TYPE_CONTAINER_OFF_MAINTENANCE =  "ContainerOffMaintenance";
	public static final String TYPE_CONTAINER_ORDER_ASSIGNED = "ContainerAssignedToOrder";
	public static final String TYPE_CONTAINER_ORDER_RELEASED = "ContainerReleasedFromOrder";
	public static final String TYPE_CONTAINER_GOOD_LOADED = "ContainerGoodLoaded";
	public static final String TYPE_CONTAINER_GOOD_UNLOADED = "ContainerGoodUnLoaded";
	public static final String TYPE_CONTAINER_ON_SHIP = "ContainerOnShip";
	public static final String TYPE_CONTAINER_OFF_SHIP = "ContainerOffShip";
	public static final String TYPE_CONTAINER_ON_TRUCK = "ContainerOnTruck";
	public static final String TYPE_CONTAINER_OFF_TRUCK = "ContainerOffTruck";
	
	public static final String TYPE_PICK_UP_SITE = "ContainerAtPickUpSite";
	public static final String TYPE_DOOR_OPEN = "ContainerDoorOpen";
	public static final String TYPE_GOODS_LOADED = "ContainerGoodsLoaded";
	public static final String TYPE_DOOR_CLOSED = "ContainerDoorClosed";
    public static final String TYPE_AT_DOCK = "ContainerAtDock";
    
    private static final Gson gson = new Gson();
    
    public ContainerEvent() {}

    public ContainerEvent(long timestampMillis, String type, String version) {
        super(timestampMillis, type, version);
    }
    
    public static ContainerEvent deserialize(String json) {
        // ContainerEvent is a concrete class just to find the type of the event
        // We could do a "normal" JSON deserialization instead
    	ContainerEvent containerEvent = gson.fromJson(json, ContainerEvent.class);
        switch (containerEvent.type) {
        case TYPE_CONTAINER_ADDED:
            return gson.fromJson(json, ContainerAddedEvent.class);
        case TYPE_CONTAINER_REMOVED:
            return gson.fromJson(json, ContainerRemovedEvent.class);
        case TYPE_CONTAINER_AT_LOCATION:
            return gson.fromJson(json, ContainerAtLocationEvent.class);
        case TYPE_CONTAINER_ON_MAINTENANCE:
            return gson.fromJson(json, ContainerOnMaintainanceEvent.class);
        case TYPE_CONTAINER_OFF_MAINTENANCE:
            return gson.fromJson(json, ContainerOffMaintainanceEvent.class);
        case TYPE_PICK_UP_SITE:
            return gson.fromJson(json, ContainerAtPickUpSiteEvent.class);
        case TYPE_DOOR_OPEN:
            return gson.fromJson(json, ContainerDoorOpenEvent.class);
        case TYPE_GOODS_LOADED:
            return gson.fromJson(json, ContainerGoodsLoadedEvent.class);
        case TYPE_DOOR_CLOSED:
            return gson.fromJson(json, ContainerDoorClosedEvent.class);
        case TYPE_AT_DOCK:
            return gson.fromJson(json, ContainerAtDockEvent.class);
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
