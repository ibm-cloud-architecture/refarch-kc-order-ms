package ibm.labs.kc.order.command.dto;

import org.junit.Test;

import ibm.labs.kc.order.command.model.Address;

public class CreateOrderRequestTest {
	
	private Address mockAddress = new Address("Street", "City", "County", "State", "Zipcode"); 

    @Test
    public void testValidateOK() {
        CreateOrderRequest cor = new CreateOrderRequest();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setProductID("myProductID");
        cor.setQuantity(100);
        cor.setCustomerID("customerID");
        cor.setDestinationAddress(mockAddress);
        cor.setPickupAddress(mockAddress);
        CreateOrderRequest.validate(cor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateBadExpectedDeliveryDate() {
        CreateOrderRequest cor = new CreateOrderRequest();
        cor.setExpectedDeliveryDate("2019-01-15T17");
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setProductID("myProductID");
        cor.setQuantity(100);
        cor.setCustomerID("customerID");
        cor.setDestinationAddress(mockAddress);
        cor.setPickupAddress(mockAddress);
        CreateOrderRequest.validate(cor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateBadPickupDate() {
        CreateOrderRequest cor = new CreateOrderRequest();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setPickupDate("2019-01-15T17");
        cor.setProductID("myProductID");
        cor.setQuantity(100);
        cor.setCustomerID("customerID");
        cor.setDestinationAddress(mockAddress);
        cor.setPickupAddress(mockAddress);
        CreateOrderRequest.validate(cor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuantity() {
        CreateOrderRequest cor = new CreateOrderRequest();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setProductID("myProductID");
        cor.setQuantity(-100);
        cor.setCustomerID("customerID");
        cor.setDestinationAddress(mockAddress);
        cor.setPickupAddress(mockAddress);
        CreateOrderRequest.validate(cor);
    }
}
