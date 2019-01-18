package ibm.labs.kc.order.command.model;

public class OrderEvent {
    
    public static final String TYPE_CREATED = "OrderCreated";

    private long timestampMillis;
    private String type;
    private String version;
    private Order payload;

    public OrderEvent(long timestampMillis, String type, String version, Order payload) {
        super();
        this.timestampMillis = timestampMillis;
        this.type = type;
        this.version = version;
        this.payload = payload;
    }

    public OrderEvent() {
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

    public Order getPayload() {
        return payload;
    }

    public void setPayload(Order payload) {
        this.payload = payload;
    }
}
