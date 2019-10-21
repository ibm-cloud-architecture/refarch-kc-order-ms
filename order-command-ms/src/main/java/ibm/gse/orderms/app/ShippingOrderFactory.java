package ibm.gse.orderms.app;

import java.util.UUID;

import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.domain.model.order.ShippingOrder;

public class ShippingOrderFactory {

	public static ShippingOrder createNewShippingOrder(ShippingOrderCreateParameters dto) {
		ShippingOrder order = new ShippingOrder(UUID.randomUUID().toString(),
                dto.getProductID(),
                dto.getCustomerID(),
                dto.getQuantity(),
                dto.getPickupAddress(), dto.getPickupDate(),
                dto.getDestinationAddress(), dto.getExpectedDeliveryDate(),
                ShippingOrder.PENDING_STATUS);
	   return order;
	}
	
}
