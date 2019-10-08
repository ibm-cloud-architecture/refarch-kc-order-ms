package ut.gse.orderms.domain.service;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.domain.service.ShippingOrderService;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.OrderEvent;
import ibm.gse.orderms.infrastructure.events.OrderEventBase;
import ibm.gse.orderms.infrastructure.kafka.KafkaInfrastructureConfig;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.KafkaConsumerMockup;
import ut.OrderCommandEventProducerMock;
import ut.OrderEventEmitterMock;
import ut.ShippingOrderTestDataFactory;

public class TestOrderService {

	public static ShippingOrderRepository orderRepository = new ShippingOrderRepositoryMock();
	
	
	@Test
	public void shouldEmitEventOnOrderCreation() {
		OrderCommandEventProducerMock commandEventProducer = new OrderCommandEventProducerMock(orderRepository);
		
		ShippingOrderService service = new ShippingOrderService(commandEventProducer, 
				orderRepository);
		Assert.assertFalse(commandEventProducer.eventEmitted);
		
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		service.createOrder(order);
		
		Assert.assertTrue(commandEventProducer.eventEmitted);
		OrderEventBase createOrderEvent = commandEventProducer.getEventEmitted();
		
		Assert.assertNotNull(createOrderEvent);
		Assert.assertTrue(OrderCommandEvent.TYPE_CREATE_ORDER.equals(createOrderEvent.getType()));
		OrderCommandEvent  orderCommand = (OrderCommandEvent)createOrderEvent;
		Assert.assertTrue(order.getOrderID().equals(((ShippingOrder)orderCommand.getPayload()).getOrderID()));
		commandEventProducer.safeClose();
		Assert.assertFalse(commandEventProducer.eventEmitted);
		
	}
	
	@Test
	public void shouldEmitEventOnOrderUpdate() {
		OrderCommandEventProducerMock commandEventProducer = new OrderCommandEventProducerMock(orderRepository);
		ShippingOrderService service = new ShippingOrderService(commandEventProducer, 
				orderRepository);
		
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		service.createOrder(order);
		commandEventProducer.safeClose();
		order.setQuantity(100);
		service.updateShippingOrder(order);
		Assert.assertTrue(commandEventProducer.eventEmitted);
		OrderEventBase orderUpdatedEvent = commandEventProducer.getEventEmitted();
		Assert.assertNotNull(orderUpdatedEvent);
		Assert.assertTrue(OrderCommandEvent.TYPE_UPDATE_ORDER.equals(orderUpdatedEvent.getType()));
		OrderCommandEvent  orderCommand = (OrderCommandEvent)orderUpdatedEvent;
		Assert.assertTrue(order.getOrderID().equals(((ShippingOrder)orderCommand.getPayload()).getOrderID()));
	}
	

	// ATTENTION this test just validates the mockup as the update in the repository is done by the agent
	@Test
	public void shouldGetCreatedOrder() {
		OrderCommandEventProducerMock commandEventProducer = new OrderCommandEventProducerMock(orderRepository);
		ShippingOrderService service = new ShippingOrderService(commandEventProducer, 
				orderRepository);
		
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		service.createOrder(order);
		Optional<ShippingOrder> persistedOrder = service.getOrderByOrderID(order.getOrderID());
		Assert.assertTrue(persistedOrder.isPresent());
	}
	
	/**
	 * The following test is more complex in settings. 
	 * The service will publish event so we use a command event producer mock
	 * The mock uses the repository as a mean to share data with consumer
	 * The Agent uses an command event consumer, which is also a mock
	 * For each command event received, handle the event so persist to the repository
	 * and then emit Order Events as facts
	 * BE SURE to add KAFKA_BROKERS as environment variable in the run configuration in eclipse.
	 * In maven the env is set in the pom.xml within unit test element
	 */
	@Test
	public void shouldGetOrderFromRepository() {
		// we need the command event consumer mock and event emitter with a real agent
		KafkaConsumerMockup<String,String> kcm = new KafkaConsumerMockup<String,String>(KafkaInfrastructureConfig.getConsumerProperties("test-grp","test-id",true,"earliest"),"orderCommands");	
		OrderEventEmitterMock orderEventEmitter = new OrderEventEmitterMock();
		
		// agent consume command events and generate order event
		OrderCommandAgent orderCommandAgent = new OrderCommandAgent(orderRepository,kcm,orderEventEmitter);
		
		// need mockup emitter
		OrderCommandEventProducerMock eventEmitter = new OrderCommandEventProducerMock(orderRepository);
		// inject emitter and repo on the service to test
		ShippingOrderService service = new ShippingOrderService(eventEmitter, 
				orderRepository);
		
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		service.createOrder(order);
		// well the repository was updated by the mockup command event emmitter, so let trick it by reset
		orderRepository.reset();
		
		// now mockup the event going to message broker
		kcm.setKey(order.getOrderID());
		String eventAsString = new Gson().toJson(eventEmitter.getEventEmitted());
		kcm.setValue(eventAsString);
		
		// the next 3 lines are simulating the runnable thread polling command events and processing them
		List<OrderCommandEvent> results = orderCommandAgent.poll();
		for (OrderCommandEvent event : results) {
           	orderCommandAgent.handle(event);
        }
		// the handle persists in the repo... so following should work 
		Optional<ShippingOrder> persistedOrder = service.getOrderByOrderID(order.getOrderID());
		Assert.assertTrue(persistedOrder.isPresent());
	
	}
}
