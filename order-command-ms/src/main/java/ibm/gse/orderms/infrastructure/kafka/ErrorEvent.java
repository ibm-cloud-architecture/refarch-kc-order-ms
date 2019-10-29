package ibm.gse.orderms.infrastructure.kafka;

import ibm.gse.orderms.infrastructure.events.OrderEventBase;
import ibm.gse.orderms.infrastructure.events.ShippingOrderPayload;

public class ErrorEvent extends OrderEventBase {

    public static final String TYPE_ERROR = "Error";

    private String errorMessage;
    private ShippingOrderPayload payload;
    
    public ErrorEvent() {
        super();
    }

    public ErrorEvent(long timestampMillis,  String version, 
    		ShippingOrderPayload payload, String errorMessage) {
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

	
	public ShippingOrderPayload getPayload() {
		return this.payload;
	}

}
