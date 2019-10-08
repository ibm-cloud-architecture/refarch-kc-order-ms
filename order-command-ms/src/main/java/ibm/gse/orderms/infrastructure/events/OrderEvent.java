package ibm.gse.orderms.infrastructure.events;

/**
 * Order event for the state change of a shipping order
 * 
 * Events are data element, so limit inheritance and polymorphism to the minimum
 * @author jeromeboyer
 *
 */
public class OrderEvent extends OrderEventBase {

   
    private ShippingOrderPayload payload;

    public OrderEvent(long timestampMillis, String type, String version, ShippingOrderPayload payload) {
        super(timestampMillis, type, version);
        this.payload = payload;
    }

    public OrderEvent() {}


    
    public ShippingOrderPayload getPayload() {
        return this.payload;
    }

}
