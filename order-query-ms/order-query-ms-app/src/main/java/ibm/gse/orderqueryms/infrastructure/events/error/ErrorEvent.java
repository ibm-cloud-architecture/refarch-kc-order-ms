package ibm.gse.orderqueryms.infrastructure.events.error;

import ibm.gse.orderqueryms.infrastructure.events.AbstractEvent;
import ibm.gse.orderqueryms.infrastructure.events.Event;

public class ErrorEvent extends AbstractEvent {

    public static final String TYPE_ERROR = "Error";

    private Event payload;
    private String errorMessage;

    public ErrorEvent() {
        super();
    }

    public ErrorEvent(long timestampMillis, String type, String version, Event payload, String errorMessage) {
        super(timestampMillis, type, version);
        this.payload = payload;
        this.errorMessage = errorMessage;
    }

    @Override
    public Event getPayload() {
        return payload;
    }

    public void setPayload(Event payload) {
        this.payload = payload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
