package ut.orderms.infrastructure;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.OrderEvent;
import ibm.gse.orderms.infrastructure.kafka.ErrorEvent;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.KafkaConsumerMockup;
import ut.OrderEventEmitterMock;
import ut.ShippingOrderTestDataFactory;

/**
 * Test order command agent with consumer mock
 * 
 * When a POST operation is done to create an order, the commmand microservice
 * creates a create order command, and posts it to the orderCommands topic.
 * 
 * As an agent it listen to command events. So validate the processing of those command events
 * 
 * Two types of event implemented so far: create order and update order
 *  
 * @author jerome boyer
 *
 */
public class TestOrderCommandAgent {
	
	static OrderCommandAgent agent = null;
	static ShippingOrderRepositoryMock repository = null ;
	static KafkaConsumerMockup<String,String> orderCommandsConsumerMock = null;
	static OrderEventEmitterMock orderEventProducerMock = null;
	static OrderEventEmitterMock errorEventProducerMock = null;
	
	@BeforeClass
	public static void createMockups() {
		if (orderCommandsConsumerMock == null) {
			Properties properties = ShippingOrderTestDataFactory.buildConsumerKafkaProperties();
			orderCommandsConsumerMock = new KafkaConsumerMockup<String,String>(properties,"orderCommands");	
		}
		if ( orderEventProducerMock == null) {
			orderEventProducerMock = new OrderEventEmitterMock();
		}
		if ( errorEventProducerMock == null) {
			errorEventProducerMock = new OrderEventEmitterMock();
		}
		repository = new ShippingOrderRepositoryMock();
	}
	
	@Before
	public void createAgent() {		
		agent = new OrderCommandAgent(repository,orderCommandsConsumerMock,orderEventProducerMock,errorEventProducerMock);
	}
	
	@After
	public void resetMockups() {
		orderEventProducerMock.emittedEvent = null;
		orderEventProducerMock.eventEmitted = false;
		errorEventProducerMock.eventEmitted = false;
		errorEventProducerMock.emittedEvent = null;
		orderEventProducerMock.failure = false;
		errorEventProducerMock.failure = false;
	}
	
	/**
	 * Validate the agent is consuming command event for create order, persist
	 * and generate event 
	 */

	@Test
	public void shouldPersistOrderAndGenerateOrderCreatedEvent() {
		orderCommandsConsumerMock.setValue("{\"payload\":{\"orderID\":\"Order01\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":10,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"CreateOrderCommand\"}");
		orderCommandsConsumerMock.setKey("Order01");
		List<OrderCommandEvent> results = agent.poll();
		assertNotNull(results);
		assertNotNull(results.get(0));
		assertNotNull(results.get(0).getPayload());
		OrderCommandEvent createOrderEvent = results.get(0);
		assertNotNull(createOrderEvent);
		Assert.assertTrue(OrderCommandEvent.TYPE_CREATE_ORDER.contentEquals(createOrderEvent.getType()));
		
		ShippingOrder shippingOrder = (ShippingOrder)results.get(0).getPayload();
		Assert.assertTrue("FreshCarrots".equals(shippingOrder.getProductID()));
		// should persist the order via the handler
		agent.handle(createOrderEvent);
		// verify order is persisted
		Optional<ShippingOrder> oso = repository.getOrderByOrderID("Order01");
		Assert.assertTrue(oso.isPresent());
		Assert.assertTrue(oso.get().getCustomerID().equals("Farm01"));
		// now verify it generates event
		Assert.assertTrue(orderEventProducerMock.eventEmitted);
		Assert.assertTrue(orderEventProducerMock.emittedEvent.getType().equals(OrderEvent.TYPE_ORDER_CREATED));
	
	}
	
	@Test
	public void shouldUpdateOrderAndEmitEvent() {
		orderCommandsConsumerMock.setValue("{\"payload\":{\"orderID\":\"Order01\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":10,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"CreateOrderCommand\"}");
		orderCommandsConsumerMock.setKey("Order01");
		List<OrderCommandEvent> results = agent.poll();
		OrderCommandEvent createOrderEvent = results.get(0);
		agent.handle(createOrderEvent);
		Optional<ShippingOrder> oso = repository.getOrderByOrderID("Order01");
		Assert.assertTrue(oso.isPresent());
		Assert.assertTrue(oso.get().getQuantity() == 10);
		// create an update event
		orderCommandsConsumerMock.setValue("{\"payload\":{\"orderID\":\"Order01\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":30,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"UpdateOrderCommand\"}");
		orderCommandsConsumerMock.setKey("Order01");
		results = agent.poll();
		OrderCommandEvent updateOrderEvent = results.get(0);
		assertNotNull(updateOrderEvent);
		// should persist the order via the handler
		agent.handle(updateOrderEvent);
		// verify order is persisted
		oso = repository.getOrderByOrderID("Order01");
		Assert.assertTrue(oso.isPresent());
		Assert.assertTrue(oso.get().getQuantity() == 30);
		// verify order updated event
		Assert.assertTrue(orderEventProducerMock.eventEmitted);
		Assert.assertTrue(orderEventProducerMock.emittedEvent.getType().equals(OrderEvent.TYPE_ORDER_UPDATED));

	}
	
	@Test
	// not very useful as we test the mockup here. but it is cool to test the mockup too
	public void shouldTimeOutOnPoll() {
		orderCommandsConsumerMock.enforceTimeOut();
		List<OrderCommandEvent> results = agent.poll();
		Assert.assertTrue(results.isEmpty());
		orderCommandsConsumerMock.resetTimeOut();
		
	}
	
	@Test
	/**
	 * when repository is not saving. No orderCreated event is generated
	 * but error event is emitted.
	 */
	public void shouldGenerateErrorEventOnRepositoryFailureWhileCreating() {
		repository.injectFailure();
		// send the order create command event
		orderCommandsConsumerMock.setValue("{\"payload\":{\"orderID\":\"Order03\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":10,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"CreateOrderCommand\"}");
		orderCommandsConsumerMock.setKey("Order03");
		List<OrderCommandEvent> results = agent.poll();
		OrderCommandEvent createOrderEvent = results.get(0);
		agent.handle(createOrderEvent);
		Assert.assertFalse(orderEventProducerMock.eventEmitted);
		Assert.assertTrue(errorEventProducerMock.eventEmitted);
		ErrorEvent ee = (ErrorEvent)errorEventProducerMock.emittedEvent;
		Assert.assertTrue(ee.getPayload().getOrderID().equals("Order03"));
		repository.resetNormalOperation();
	}

	@Test
	/**
	 * Same as above but for update
	 */
	public void shouldGenerateErrorOnUpdateFailingToPersist() {
		// be sure to have one order in repository with the same id
		orderCommandsConsumerMock.setValue("{\"payload\":{\"orderID\":\"Order11\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":10,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"CreateOrderCommand\"}");
		orderCommandsConsumerMock.setKey("Order01");
		List<OrderCommandEvent> results = agent.poll();
		OrderCommandEvent createOrderEvent = results.get(0);
		agent.handle(createOrderEvent);
		// be sure to reset the mockup
		orderEventProducerMock.emittedEvent = null;
		orderEventProducerMock.eventEmitted = false;
		// now set the repository in failure and send an update event
		repository.injectFailure();
		orderCommandsConsumerMock.setValue("{\"payload\":{\"orderID\":\"Order11\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":30,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"UpdateOrderCommand\"}");
		orderCommandsConsumerMock.setKey("Order11");
		results = agent.poll();
		OrderCommandEvent updateOrderEvent = results.get(0);
		agent.handle(updateOrderEvent);
		Assert.assertFalse(orderEventProducerMock.eventEmitted);
		Assert.assertTrue(errorEventProducerMock.eventEmitted);
		ErrorEvent ee = (ErrorEvent)errorEventProducerMock.emittedEvent;
		Assert.assertTrue(ee.getPayload().getOrderID().equals("Order11"));
		repository.resetNormalOperation();
	}

	@Test
	public void shouldStopRunningWhenItCouldNotEmitEventOnOrderCreation() {
		Assert.assertTrue(agent.isRunning());
		orderCommandsConsumerMock.setValue("{\"payload\":{\"orderID\":\"Order11\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":10,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"CreateOrderCommand\"}");
		orderCommandsConsumerMock.setKey("Order01");
		List<OrderCommandEvent> results = agent.poll();
		OrderCommandEvent createOrderEvent = results.get(0);
		// inject communication error in kafka
		orderEventProducerMock.failure = true;
		agent.handle(createOrderEvent);
		Assert.assertFalse(orderEventProducerMock.eventEmitted);
		Assert.assertFalse(errorEventProducerMock.eventEmitted);
		Assert.assertFalse(agent.isRunning());
	}
	
	
	@Test
	public void shouldStopRunningWhenItCouldNotEmitEventOnOrderUpdate() {
		Assert.assertTrue(agent.isRunning());
		orderCommandsConsumerMock.setValue("{\"payload\":{\"orderID\":\"Order11\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":10,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"UpdateOrderCommand\"}");
		orderCommandsConsumerMock.setKey("Order01");
		List<OrderCommandEvent> results = agent.poll();
		OrderCommandEvent updateOrderEvent = results.get(0);
		// inject communication error in kafka
		orderEventProducerMock.failure = true;
		agent.handle(updateOrderEvent);
		Assert.assertFalse(orderEventProducerMock.eventEmitted);
		Assert.assertFalse(errorEventProducerMock.eventEmitted);
		Assert.assertFalse(agent.isRunning());
	}
}
