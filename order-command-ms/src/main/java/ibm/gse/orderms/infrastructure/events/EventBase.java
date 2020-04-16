package ibm.gse.orderms.infrastructure.events;

/**
 * This is the common part of the order events. 
 * Events are data element, so limit inheritance and polymorphism.
 * @author jerome boyer
 *
 */
public class EventBase {

	 public static final String TYPE_ORDER_CREATED = "OrderCreated";
	 public static final String TYPE_ORDER_UPDATED = "OrderUpdated";
     public static final String TYPE_VOYAGE_ASSIGNED = "VoyageAssigned"; // from voyage ms
     public static final String TYPE_VOYAGE_NOT_FOUND = "VoyageNotFound"; // from voyage ms
	 public static final String TYPE_ORDER_REJECTED = "OrderRejected";
     public static final String TYPE_ORDER_CANCELLED = "OrderCancelled";
     public static final String TYPE_ORDER_SPOILT = "OrderSpoilt"; // from containers ms
	   
	 public static final String TYPE_CONTAINER_ALLOCATED = "ContainerAllocated";
     public static final String TYPE_CONTAINER_NOT_FOUND = "ContainerNotFound";
    

	    
    protected long timestampMillis;
    protected String type;
    protected String version;

    public EventBase() {
    }

    public EventBase(long timestampMillis, String type, String version) {
        this.timestampMillis = timestampMillis;
        this.type = type;
        this.version = version;
    }
    
    
    
    public long getTimestampMillis() {
        return timestampMillis;
    }

    
    public void setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    
    public String getType() {
        return type;
    }

    
    public void setType(String type) {
        this.type = type;
    }

    
    public void setVersion(String version) {
        this.version = version;
    }

    
    public String getVersion() {
        return version;
    }
}
