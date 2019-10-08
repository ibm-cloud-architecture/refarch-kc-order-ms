package ibm.gse.orderms.infrastructure.kafka;

import ibm.gse.orderms.infrastructure.events.OrderEventBase;

public class ErrorEvent extends OrderEventBase {

    public static final String TYPE_ERROR = "Error";

    private String errorMessage;
    private OrderEventBase payload;
    
    public ErrorEvent() {
        super();
    }

    public ErrorEvent(long timestampMillis, String type, String version, OrderEventBase payload, String errorMessage) {
        super(timestampMillis, type, version);
        this.payload = payload;
        this.errorMessage = errorMessage;
    }



    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

	
	public OrderEventBase getPayload() {
		return this.payload;
	}

}
