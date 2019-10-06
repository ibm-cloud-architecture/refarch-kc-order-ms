package ut;

import java.util.UUID;

import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.app.dto.ShippingOrderUpdateParameters;
import ibm.gse.orderms.domain.model.order.Address;
import ibm.gse.orderms.domain.model.order.ShippingOrder;

/**
 * A factory to generate test fixtures
 * 
 * @author jerome boyer
 *
 */
public class ShippingOrderTestDataFactory {

	
	public static ShippingOrder orderFixtureWithoutIdentity() {
		
		Address pickupAddress = new Address("Street", "City", "OriginCountry", "State", "Zipcode");
		Address destinationAddress = new Address("Street", "City", "DestinationCountry", "State", "Zipcode");
		ShippingOrder order = new ShippingOrder("","P01","AFarmer",100, pickupAddress,
				"2019-01-14T17:48Z", 
				destinationAddress, "2019-01-15T17:48Z",  ShippingOrder.PENDING_STATUS);
		return order;
	}
	
	public static ShippingOrderCreateParameters orderCreateFixtureWithoutID() {
		ShippingOrderCreateParameters dto = new ShippingOrderCreateParameters();
		dto.setCustomerID("AtestCustomer");
		dto.setProductID("P03");
		Address mockAddress = new Address("Street", "City", "Country", "State", "Zipcode");
		dto.setExpectedDeliveryDate("2019-01-15T17:48Z");
		dto.setPickupDate("2019-01-14T17:48Z");
		dto.setQuantity(100);
		dto.setCustomerID("customerID");
		dto.setDestinationAddress(mockAddress);
		dto.setPickupAddress(mockAddress);
		return dto;
	}
	
	public static ShippingOrder orderFixtureWithIdentity() {
		ShippingOrder order = 	orderFixtureWithoutIdentity();
		order.setOrderID(UUID.randomUUID().toString());
		return order;
	}
	
	
	public static ShippingOrderUpdateParameters updateOrderFixtureFromOrder(ShippingOrder existingOrder) {
		ShippingOrderUpdateParameters updateParameters = new ShippingOrderUpdateParameters();
		updateParameters.setOrderID(existingOrder.getOrderID());
		updateParameters.setProductID(existingOrder.getProductID());
		updateParameters.setCustomerID(existingOrder.getCustomerID());
		updateParameters.setStatus(existingOrder.getStatus());
		updateParameters.setQuantity(existingOrder.getQuantity());
		updateParameters.setPickupAddress(existingOrder.getPickupAddress());
		updateParameters.setPickupDate(existingOrder.getPickupDate());
		updateParameters.setDestinationAddress(existingOrder.getDestinationAddress());
		updateParameters.setExpectedDeliveryDate(existingOrder.getExpectedDeliveryDate());
		return updateParameters;
	}
}
