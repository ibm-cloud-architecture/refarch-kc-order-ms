package ut.orderms.app;

import static org.mockito.Mockito.mock;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ibm.gse.orderms.app.ShippingOrderResource;
import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.domain.service.ShippingOrderService;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandEmitter;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.ShippingOrderTestDataFactory;

/**
 * There are a few commands (from event storming point of view) that we want to test
 * at the exposed API level.
 * @author jerome boyer
 *
 */
public class TestOrderResource  {

	static EventEmitter orderCommandProducerMock = mock(OrderCommandEmitter.class);
	static ShippingOrderService serv;
	static ShippingOrderResource resource;
	
	@BeforeClass
	public static void prepareTests() {
		serv = new ShippingOrderService(orderCommandProducerMock,ShippingOrderRepositoryMock.instance());
		resource = new ShippingOrderResource(serv);
	}

	
	@Test
	public void shouldRejectOrderCreationForOrderWithoutProductID() {
		ShippingOrderCreateParameters orderDTO = new ShippingOrderCreateParameters();
		Response rep = resource.createOrder(orderDTO);
		Assert.assertNotNull(rep);
		Assert.assertTrue(rep.getStatus() == 400);	
		Assert.assertTrue(rep.getStatusInfo().getReasonPhrase().contains("Product ID"));
	}
	
	
	@Test
	public void shouldRejectOrderCreationForOrderWithoutCustomerID() {
		ShippingOrderCreateParameters orderDTO = new ShippingOrderCreateParameters();
		orderDTO.setProductID("TestProduct");
		Response rep = resource.createOrder(orderDTO);
		Assert.assertNotNull(rep);
		Assert.assertTrue(rep.getStatus() == 400);
		Assert.assertTrue(rep.getStatusInfo().getReasonPhrase().contains("Customer ID"));
		
	}
	
	@Test
	public void shouldCreateOrder() {
		ShippingOrderCreateParameters orderDTO = ShippingOrderTestDataFactory.orderCreateFixtureWithoutID();
		Response rep = resource.createOrder(orderDTO);
		Assert.assertNotNull(rep);
		Assert.assertTrue(rep.getStatus() == 200);	
		String orderID=((String)rep.getEntity());
		ShippingOrderRepository repository = ShippingOrderRepositoryMock.instance();
		repository.getByID(orderID);
	}

}
