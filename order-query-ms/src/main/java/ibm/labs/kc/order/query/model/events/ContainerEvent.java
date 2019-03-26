package ibm.labs.kc.order.query.model.events;

import com.google.gson.Gson;

public class ContainerEvent extends AbstractEvent {
	
	public static final String TYPE_AVAILABLE = "ContainerAvailable, AssignedToOrder";
	public static final String TYPE_PICK_UP_SITE = "ContainerAtPickUpSite";
	public static final String TYPE_DOOR_OPEN = "ContainerDoorOpen";
    public static final String TYPE_ASSIGNED = "ContainerAssigned";
    
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
        case TYPE_AVAILABLE:
            return gson.fromJson(json, AvailableContainerEvent.class);
        case TYPE_PICK_UP_SITE:
            return gson.fromJson(json, ContainerAtPickUpSiteEvent.class);
        case TYPE_DOOR_OPEN:
            return gson.fromJson(json, ContainerDoorOpenEvent.class);
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
