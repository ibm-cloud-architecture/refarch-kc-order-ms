package ibm.gse.orderqueryms.infrastructure.events.container;

import com.google.gson.Gson;

import ibm.gse.orderqueryms.infrastructure.events.AbstractEvent;

public class ContainerEvent extends AbstractEvent {
	
	public static final String TYPE_CONTAINER_ADDED = "ContainerAdded";
	public static final String TYPE_CONTAINER_ON_MAINTENANCE = "ContainerOnMaintenance";
	public static final String TYPE_CONTAINER_OFF_MAINTENANCE =  "ContainerOffMaintenance";
	public static final String TYPE_CONTAINER_ORDER_ASSIGNED = "ContainerAssignedToOrder";
    
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
        case TYPE_CONTAINER_ON_MAINTENANCE:
            return gson.fromJson(json, ContainerOnMaintenanceEvent.class);
        case TYPE_CONTAINER_OFF_MAINTENANCE:
            return gson.fromJson(json, ContainerOffMaintenanceEvent.class);
        case TYPE_CONTAINER_ORDER_ASSIGNED:
            return gson.fromJson(json, ContainerOrderAssignedEvent.class);
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
