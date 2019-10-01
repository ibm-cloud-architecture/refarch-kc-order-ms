package ibm.gse.orderms.app.dto;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class ShippingOrderCreateParameters extends ShippingOrderParameters {

	
    public static void validateInputData(ShippingOrderParameters co) throws IllegalArgumentException{
        if (co.getProductID() == null) {
            throw new IllegalArgumentException("Product ID is null");
        }
        if (co.getCustomerID() == null) {
            throw new IllegalArgumentException("Customer ID is null");
        }
        if (co.getExpectedDeliveryDate() == null) {
            throw new IllegalArgumentException("Expected delivery date is null");
        }
        if (co.getPickupDate() == null) {
            throw new IllegalArgumentException("Pickup date is null");
        }
        if (co.getPickupAddress() == null) {
            throw new IllegalArgumentException("Pickup address is null");
        }
        if (co.getDestinationAddress() == null) {
            throw new IllegalArgumentException("Destination address is null");
        }

        try {
            OffsetDateTime.parse(co.getExpectedDeliveryDate(), DateTimeFormatter.ISO_DATE_TIME);
            OffsetDateTime.parse(co.getPickupDate(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (RuntimeException rex) {
            throw new IllegalArgumentException(rex);
        }

        if (co.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}
