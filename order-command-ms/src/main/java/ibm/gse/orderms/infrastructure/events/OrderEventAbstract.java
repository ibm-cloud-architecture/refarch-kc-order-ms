package ibm.gse.orderms.infrastructure.events;

public abstract class OrderEventAbstract {

    protected long timestampMillis;
    protected String type;
    protected String version;


    public OrderEventAbstract() {
    }

    public OrderEventAbstract(long timestampMillis, String type, String version) {
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

	public abstract Object getPayload();

}
