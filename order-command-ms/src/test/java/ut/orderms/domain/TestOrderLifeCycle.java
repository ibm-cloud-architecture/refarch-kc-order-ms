package ut.orderms.domain;

import org.junit.Before;
import org.junit.Test;

import ibm.gse.orderms.app.AppRegistry;
import ibm.gse.orderms.domain.model.order.Address;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.domain.service.ShippingOrderService;
import ut.OrderCommandEventProducerMock;

public class TestOrderLifeCycle {
	
	 
	 static ShippingOrderService serv;
	 static OrderCommandEventProducerMock orderProducerMock ;
	
	 @Before
	 public  void setUpBeforeTest() throws Exception {
		 orderProducerMock = new OrderCommandEventProducerMock();
		 serv = new ShippingOrderService(orderProducerMock, AppRegistry.getInstance().shippingOrderRepository());
	 }

	@Test
	public void shouldCreateOrderAndSendEvent() {
		Address mockAddress = new Address("Street", "City", "County", "State", "Zipcode");
		ShippingOrder cor = new ShippingOrder(
				"Order01",
				"FreshCarrots",
				"Farm01",
				10,
				mockAddress,
				"2019-01-14T17:48Z",
				mockAddress,
				"2019-03-15T17:48Z",
				ShippingOrder.PENDING_STATUS
				);
		// because create order is asynchronous for the event 
		orderProducerMock.defineEqualAssertion("OrderID","Order01");
		serv.createOrder(cor);
		
	}

}
