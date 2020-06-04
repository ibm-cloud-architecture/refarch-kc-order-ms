package ibm.gse.orderqueryms.infrastructure.events;

public abstract class AbstractEvent implements Event {

    protected long timestampMillis;
    protected String type;
    protected String version;

    public AbstractEvent() {
    }

    public AbstractEvent(long timestampMillis, String type, String version) {
        this.timestampMillis = timestampMillis;
        this.type = type;
        this.version = version;
    }

    @Override
    public long getTimestampMillis() {
        return timestampMillis;
    }

    @Override
    public void setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

}
