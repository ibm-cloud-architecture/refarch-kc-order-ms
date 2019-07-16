package ut;

import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.gson.Gson;

import ibm.labs.kc.order.command.dao.OrderDAO;
import ibm.labs.kc.order.command.dto.OrderCreate;
import ibm.labs.kc.order.command.model.Address;
import ibm.labs.kc.order.command.model.Order;
import ibm.labs.kc.order.command.model.events.EventEmitter;
import ibm.labs.kc.order.command.service.OrderCRUDService;

public class TestOrderLifeCycle {

	 @Mock
	 static EventEmitter orderProducerMock;
	 
	 @Mock
	 static OrderDAO dao;
	 
	 @Rule public MockitoRule mockitoRule = MockitoJUnit.rule(); 
	 
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void shouldCreateOrderAndSendEvent() {
		Address mockAddress = new Address("Street", "City", "County", "State", "Zipcode");
		OrderCRUDService serv = new OrderCRUDService(orderProducerMock, dao);
		OrderCreate cor = new OrderCreate();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setProductID("myProductID");
        cor.setQuantity(100);
        cor.setCustomerID("customerID");
        cor.setDestinationAddress(mockAddress);
        cor.setPickupAddress(mockAddress);
		Response rep = serv.create(cor);
		Assert.assertTrue(rep.hasEntity());
		Order order = rep.readEntity(Order.class);
		Assert.assertNotNull(order);
		Assert.assertTrue(order.getProductID().equals(cor.getProductID()));
	}

}
