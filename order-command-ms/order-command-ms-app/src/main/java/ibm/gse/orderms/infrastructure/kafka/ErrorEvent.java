package ibm.gse.orderms.infrastructure.kafka;

import ibm.gse.orderms.infrastructure.events.EventBase;
import ibm.gse.orderms.infrastructure.events.order.OrderEventPayload;

public class ErrorEvent extends EventBase {

    public static final String TYPE_ERROR = "Error";

    private String errorMessage;
    private OrderEventPayload payload;
    
    public ErrorEvent() {
        super();
    }

    public ErrorEvent(long timestampMillis,  String version, 
    		OrderEventPayload payload, String errorMessage) {
        super(timestampMillis, TYPE_ERROR, version);
        this.payload = payload;
        this.errorMessage = errorMessage;
    }



    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

	
	public OrderEventPayload getPayload() {
		return this.payload;
	}

}
