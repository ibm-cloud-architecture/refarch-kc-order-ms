package ut.orderms.app;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ibm.gse.orderms.app.ShippingOrderResource;
import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.app.dto.ShippingOrderUpdateParameters;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.domain.service.ShippingOrderService;
import ibm.gse.orderms.infrastructure.AppRegistry;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.OrderCommandEventProducerMock;
import ut.ShippingOrderTestDataFactory;

/**
 * There are a few commands (from event storming point of view) that we want to test
 * at the exposed API level.
 * @author jerome boyer
 *
 */
public class TestOrderResource  {

	static ShippingOrderService serv;
	static ShippingOrderResource resource;
	static ShippingOrderRepository orderRepository = new ShippingOrderRepositoryMock();

	/**
	 * Need to inject mockup for event emitter to avoid dependency with external messaging middleware
	 */
	@Before
	public void prepareTests() {
		EventEmitter orderCommandProducerMock = new OrderCommandEventProducerMock(orderRepository);
		serv = new ShippingOrderService(orderCommandProducerMock,
				orderRepository);
		resource = new ShippingOrderResource(serv);
	}

	
	/*
	 * Happy path for order creation -> validate the repository and the HTTP response
	 */
	@Test
	public void shouldCreateOrder() {
		ShippingOrderCreateParameters orderDTO = ShippingOrderTestDataFactory.orderCreateFixtureWithoutID();
		Response rep = resource.createShippingOrder(orderDTO);
		Assert.assertNotNull(rep);
		Assert.assertTrue(rep.getStatus() == 200);	
		String orderID=((String)rep.getEntity());
		Assert.assertNotNull(orderID);
		// in fact we do not need to validate the repository as it should be done within the service testing
		Optional<ShippingOrder> order =orderRepository.getOrderByOrderID(orderID);
		Assert.assertTrue(order.isPresent());
	}
	

	@Test
	public void shouldReturn200ForUpdateOrder() {
		// prepare data for test
		ShippingOrderCreateParameters orderDTO = ShippingOrderTestDataFactory.orderCreateFixtureWithoutID();
		Response rep = resource.createShippingOrder(orderDTO);
		String orderID=((String)rep.getEntity());
		Assert.assertNotNull(orderID);
		// Get the shipping order
		ShippingOrder existingOrder = (ShippingOrder)resource.getOrderByOrderId(orderID).getEntity();
		
		// now update the data (simulate a UI)
		ShippingOrderUpdateParameters updateParameters = ShippingOrderTestDataFactory.updateOrderFixtureFromOrder(existingOrder);
		rep = resource.updateExistingOrder(orderID,updateParameters);
		Assert.assertNotNull(rep);
		Assert.assertTrue(rep.getStatus() == 200);	
		Assert.assertNull(rep.getEntity());
	}
	
	
	
	
	/*
	 * test creation of order with missing data
	*/
	
	@Test
	public void shouldRejectOrderCreationForOrderWithoutProductID() {
		ShippingOrderCreateParameters orderDTO = new ShippingOrderCreateParameters();
		Response rep = resource.createShippingOrder(orderDTO);
		Assert.assertNotNull(rep);
		Assert.assertTrue(rep.getStatus() == 400);	
		Assert.assertTrue(rep.getStatusInfo().getReasonPhrase().contains("Product ID"));
	}
	
	
	@Test
	public void shouldRejectOrderCreationForOrderWithoutCustomerID() {
		ShippingOrderCreateParameters orderDTO = new ShippingOrderCreateParameters();
		orderDTO.setProductID("TestProduct");
		Response rep = resource.createShippingOrder(orderDTO);
		Assert.assertNotNull(rep);
		Assert.assertTrue(rep.getStatus() == 400);
		Assert.assertTrue(rep.getStatusInfo().getReasonPhrase().contains("Customer ID"));
		
	}
	


	@Test
	public void shouldGet404OnInvalidOrderID() {
		Response rep = resource.getOrderByOrderId("something-unknown");
		Assert.assertNotNull(rep);
		Assert.assertTrue(rep.getStatus() == 404);	
	}
	

	@Test
	public void shouldGet404ForNoRecords() {
		orderRepository.reset();
		Response rep = resource.getAllOrderReferences();
		Assert.assertNotNull(rep);
		Assert.assertTrue(rep.getStatus() == 404);	
	}
	

}
