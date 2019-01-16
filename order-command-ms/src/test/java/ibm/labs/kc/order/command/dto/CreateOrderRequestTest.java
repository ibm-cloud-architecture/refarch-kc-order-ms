package ibm.labs.kc.order.command.dto;

import org.junit.Test;

public class CreateOrderRequestTest {

    @Test
    public void testValidateOK() {
        CreateOrderRequest cor = new CreateOrderRequest();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setProductId("myProductID");
        cor.setQuantity(100);
        CreateOrderRequest.validate(cor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateBadDate() {
        CreateOrderRequest cor = new CreateOrderRequest();
        cor.setExpectedDeliveryDate("2019-01-15T17");
        cor.setProductId("myProductID");
        cor.setQuantity(100);
        CreateOrderRequest.validate(cor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateQuantity() {
        CreateOrderRequest cor = new CreateOrderRequest();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setProductId("myProductID");
        cor.setQuantity(-100);
        CreateOrderRequest.validate(cor);
    }
}
