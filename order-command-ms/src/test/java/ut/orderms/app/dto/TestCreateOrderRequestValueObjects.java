package ut.orderms.app.dto;

import org.junit.Test;

import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.domain.model.order.Address;

public class TestCreateOrderRequestValueObjects {

	private Address mockAddress = new Address("Street", "City", "County", "State", "Zipcode");

    @Test
    public void testValidateOK() {
        ShippingOrderCreateParameters cor = new ShippingOrderCreateParameters();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setProductID("myProductID");
        cor.setQuantity(100);
        cor.setCustomerID("customerID");
        cor.setDestinationAddress(mockAddress);
        cor.setPickupAddress(mockAddress);
        ShippingOrderCreateParameters.validateInputData(cor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateBadExpectedDeliveryDate() {
        ShippingOrderCreateParameters cor = new ShippingOrderCreateParameters();
        cor.setExpectedDeliveryDate("2019-01-15T17");
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setProductID("myProductID");
        cor.setQuantity(100);
        cor.setCustomerID("customerID");
        cor.setDestinationAddress(mockAddress);
        cor.setPickupAddress(mockAddress);
        ShippingOrderCreateParameters.validateInputData(cor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateBadPickupDate() {
        ShippingOrderCreateParameters cor = new ShippingOrderCreateParameters();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setPickupDate("2019-01-15T17");
        cor.setProductID("myProductID");
        cor.setQuantity(100);
        cor.setCustomerID("customerID");
        cor.setDestinationAddress(mockAddress);
        cor.setPickupAddress(mockAddress);
        ShippingOrderCreateParameters.validateInputData(cor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuantity() {
        ShippingOrderCreateParameters cor = new ShippingOrderCreateParameters();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setProductID("myProductID");
        cor.setQuantity(-100);
        cor.setCustomerID("customerID");
        cor.setDestinationAddress(mockAddress);
        cor.setPickupAddress(mockAddress);
        ShippingOrderCreateParameters.validateInputData(cor);
    }
}
