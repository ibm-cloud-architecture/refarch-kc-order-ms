package ut.orderms.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.model.order.Address;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.domain.service.ShippingOrderService;
import ibm.gse.orderms.infrastructure.command.events.CreateOrderCommandEvent;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.Event;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;

public class TestOrderLifeCycle {

	 public class OrderCommandProducerMock implements EventEmitter {
		String expectedValue = "";
		
		public void defineEqualAssertion(String key, String value) {
			expectedValue = value;
		}
		
		@Override
		public void emit(Event event) throws Exception {
			OrderCommandEvent orderEvent = (OrderCommandEvent)event;
	        String key = null;
	        String value = null;
	        switch (orderEvent.getType()) {
	        case OrderCommandEvent.TYPE_CREATE_ORDER:
	            key = ((CreateOrderCommandEvent)orderEvent).getPayload().getOrderID();
	            value = new Gson().toJson((CreateOrderCommandEvent)orderEvent);
	            break;
	        case OrderCommandEvent.TYPE_UPDATE_ORDER:
	            break;
	        default:
	            key = null;
	            value = null;
	        }
			System.out.println("Emit event -> " + key + " " + value);
			Assert.assertTrue(key.contentEquals(expectedValue));
		}

		@Override
		public void safeClose() {
			// TODO Auto-generated method stub
			
		}
		 
	 }
	 

	
	 
	 static ShippingOrderService serv;
	 static OrderCommandProducerMock orderProducerMock ;
	
	 @Before
	 public  void setUpBeforeTest() throws Exception {
		 orderProducerMock = this.new OrderCommandProducerMock();
		 serv = new ShippingOrderService(orderProducerMock, ShippingOrderRepositoryMock.instance());
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
