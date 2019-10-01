package ut;

import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.domain.model.order.Address;
import ibm.gse.orderms.domain.model.order.ShippingOrder;

public class ShippingOrderTestDataFactory {

	
	public static ShippingOrder orderFixtureWithoutIdentity() {
		ShippingOrder order = new ShippingOrder();
		Address destinationAddress = new Address();
		order.setDestinationAddress(destinationAddress);
		return order;
	}
	
	public static ShippingOrderCreateParameters orderCreateFixtureWithoutID() {
		ShippingOrderCreateParameters dto = new ShippingOrderCreateParameters();
		dto.setCustomerID("AtestCustomer");
		dto.setProductID("carots");
		Address mockAddress = new Address("Street", "City", "Country", "State", "Zipcode");
		dto.setExpectedDeliveryDate("2019-01-15T17:48Z");
		dto.setPickupDate("2019-01-14T17:48Z");
		dto.setProductID("myProductID");
		dto.setQuantity(100);
		dto.setCustomerID("customerID");
		dto.setDestinationAddress(mockAddress);
		dto.setPickupAddress(mockAddress);
		return dto;
	}
}
