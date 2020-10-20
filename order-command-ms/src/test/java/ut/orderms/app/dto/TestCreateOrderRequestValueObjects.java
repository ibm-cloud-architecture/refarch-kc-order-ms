package ut.orderms.app.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.domain.model.order.Address;

public class TestCreateOrderRequestValueObjects {

	private Address mockAddress = new Address("Street", "City", "County", "State", "Zipcode");

    private ShippingOrderCreateParameters buildGenericOrder(){
        ShippingOrderCreateParameters cor = new ShippingOrderCreateParameters();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setProductID("myProductID");
        cor.setQuantity(100);
        cor.setCustomerID("customerID");
        cor.setDestinationAddress(mockAddress);
        cor.setPickupAddress(mockAddress);
        return cor;
    }

    @Test
    public void given_good_order_it_should_have_no_exception() {
        ShippingOrderCreateParameters cor = buildGenericOrder();
        ShippingOrderCreateParameters.validateInputData(cor);
    }

    @Test
    public void given_unset_product_information_it_should_generate_exception() {
        ShippingOrderCreateParameters cor = buildGenericOrder();
        cor.setProductID(null);
        Assertions.assertThrows(IllegalArgumentException.class,
        () -> {  ShippingOrderCreateParameters.validateInputData(cor);});
    }

    @Test
    public void given_unset_customer_information_it_should_generate_exception() {
        ShippingOrderCreateParameters cor = buildGenericOrder();
        cor.setCustomerID(null);
        Assertions.assertThrows(IllegalArgumentException.class,
        () -> {  ShippingOrderCreateParameters.validateInputData(cor);});
    }

    @Test
    public void given_wrong_expected_deliver_data_format_should_lead_to_exception() {
        ShippingOrderCreateParameters cor = buildGenericOrder();
        cor.setExpectedDeliveryDate("2019-01-15T17");
        Assertions.assertThrows(IllegalArgumentException.class,
             () -> {  ShippingOrderCreateParameters.validateInputData(cor);});
    }

    @Test
    public void given_wrong_expected_pickup_data_format_should_lead_to_exception() {
        ShippingOrderCreateParameters cor = buildGenericOrder();
        cor.setPickupDate("2019-01-15T17");
        Assertions.assertThrows(IllegalArgumentException.class,
        () -> {  ShippingOrderCreateParameters.validateInputData(cor);});
    }

    @Test
    public void given_negative_quantity_it_should_generate_exception() {
        ShippingOrderCreateParameters cor = buildGenericOrder();
        cor.setQuantity(-100);
        Assertions.assertThrows(IllegalArgumentException.class,
        () -> {  ShippingOrderCreateParameters.validateInputData(cor);});
    }
}
