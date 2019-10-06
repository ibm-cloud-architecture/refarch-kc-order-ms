package ibm.gse.orderms.infrastructure.events;

public class ErrorEvent extends OrderEventAbstract {

    public static final String TYPE_ERROR = "Error";

    private String errorMessage;
    private OrderEventAbstract payload;
    
    public ErrorEvent() {
        super();
    }

    public ErrorEvent(long timestampMillis, String type, String version, OrderEventAbstract payload, String errorMessage) {
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

	@Override
	public Object getPayload() {
		return this.payload;
	}

}
