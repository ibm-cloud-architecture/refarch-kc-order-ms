package ut.orderms.app;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ibm.gse.orderms.app.ShippingOrderResource;
import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.app.dto.ShippingOrderUpdateParameters;
import ibm.gse.orderms.domain.events.EventEmitter;
import ibm.gse.orderms.domain.events.order.OrderEventPayload;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.domain.service.ShippingOrderService;
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
	@BeforeEach
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
		Assertions.assertNotNull(rep);
		Assertions.assertTrue(rep.getStatus() == 200);	
		OrderEventPayload payload = (OrderEventPayload) rep.getEntity();
		String orderID = payload.getOrderID();
		Assertions.assertNotNull(orderID);
		// in fact we do not need to validate the repository as it should be done within the service testing
		Optional<ShippingOrder> order =orderRepository.getOrderByOrderID(orderID);
		Assertions.assertTrue(order.isPresent());
	}
	

	@Test
	public void shouldReturn200ForUpdateOrder() {
		// prepare data for test
		ShippingOrderCreateParameters orderDTO = ShippingOrderTestDataFactory.orderCreateFixtureWithoutID();
		Response rep = resource.createShippingOrder(orderDTO);
		OrderEventPayload payload = (OrderEventPayload) rep.getEntity();
		String orderID = payload.getOrderID();
		Assertions.assertNotNull(orderID);
		// Get the shipping order
		ShippingOrder existingOrder = (ShippingOrder)resource.getOrderByOrderId(orderID).getEntity();
		
		// now update the data (simulate a UI)
		ShippingOrderUpdateParameters updateParameters = ShippingOrderTestDataFactory.updateOrderFixtureFromOrder(existingOrder);
		rep = resource.updateExistingOrder(orderID,updateParameters);
		Assertions.assertNotNull(rep);
		Assertions.assertTrue(rep.getStatus() == 200);	
		Assertions.assertNull(rep.getEntity());
	}
	
	
	
	
	/*
	 * test creation of order with missing data
	*/
	
	@Test
	public void shouldRejectOrderCreationForOrderWithoutProductID() {
		ShippingOrderCreateParameters orderDTO = new ShippingOrderCreateParameters();
		Response rep = resource.createShippingOrder(orderDTO);
		Assertions.assertNotNull(rep);
		Assertions.assertTrue(rep.getStatus() == 400);	
		Assertions.assertTrue(rep.getStatusInfo().getReasonPhrase().contains("Product ID"));
	}
	
	
	@Test
	public void shouldRejectOrderCreationForOrderWithoutCustomerID() {
		ShippingOrderCreateParameters orderDTO = new ShippingOrderCreateParameters();
		orderDTO.setProductID("TestProduct");
		Response rep = resource.createShippingOrder(orderDTO);
		Assertions.assertNotNull(rep);
		Assertions.assertTrue(rep.getStatus() == 400);
		Assertions.assertTrue(rep.getStatusInfo().getReasonPhrase().contains("Customer ID"));
		
	}
	


	@Test
	public void shouldGet404OnInvalidOrderID() {
		Response rep = resource.getOrderByOrderId("something-unknown");
		Assertions.assertNotNull(rep);
		Assertions.assertTrue(rep.getStatus() == 404);	
	}
	

	@Test
	public void shouldGet404ForNoRecords() {
		orderRepository.reset();
		Response rep = resource.getAllOrderReferences();
		Assertions.assertNotNull(rep);
		Assertions.assertTrue(rep.getStatus() == 404);	
	}
	

}
