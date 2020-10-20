package ut.orderms.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Properties;

import com.google.gson.Gson;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import ibm.gse.orderms.domain.events.EventBase;
import ibm.gse.orderms.domain.events.command.OrderCommandEvent;
import ibm.gse.orderms.domain.events.order.OrderEventPayload;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.domain.service.ShippingOrderService;
import ibm.gse.orderms.infrastructure.kafka.KafkaInfrastructureConfig;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.repository.OrderCreationException;
import ibm.gse.orderms.infrastructure.repository.OrderUpdateException;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.KafkaConsumerMockup;
import ut.OrderCommandEventProducerMock;
import ut.OrderEventEmitterMock;
import ut.ShippingOrderTestDataFactory;

public class TestOrderService {

	public static ShippingOrderRepository orderRepository = new ShippingOrderRepositoryMock();
	public static OrderCommandEventProducerMock commandEventProducer;
	public static ShippingOrderService service;

	@BeforeAll
	public static void defineOrderService() {
		commandEventProducer = new OrderCommandEventProducerMock(orderRepository);
		service = new ShippingOrderService(commandEventProducer, orderRepository);
	}

	@AfterEach
	public void clearEvents(){
		commandEventProducer.eventEmitted=false;
	}

	@Test
	@Order(1)
	public void order_created_should_emit_command_event() throws OrderCreationException {
		assume_there_is_no_event_emitted();
		OrderEventPayload new_order = ShippingOrderTestDataFactory.given_a_new_order();
		// when
		service.createOrder(new_order);
		// then
		assertTrue(order_created_event_generated());
		OrderCommandEvent createdOrderEvent = (OrderCommandEvent)commandEventProducer.getEventEmitted();
		assertTrue(new_order.getOrderID().equals(((OrderEventPayload)createdOrderEvent.getPayload()).getOrderID()));
	}

	private boolean order_created_event_generated() {
		assertTrue(commandEventProducer.eventEmitted);
		OrderCommandEvent createOrderEvent = (OrderCommandEvent)commandEventProducer.getEventEmitted();
		Assertions.assertNotNull(createOrderEvent);
		return OrderCommandEvent.ORDER_CREATED_TYPE.equals(createOrderEvent.getType());
	}

	@Test
	@Order(2)
	public void updated_order_should_emit_event() throws OrderCreationException, OrderUpdateException {
	
		OrderEventPayload orderPayload = ShippingOrderTestDataFactory.given_a_new_order();
		service.createOrder(orderPayload);
		orderPayload.setQuantity(100);
		
		service.updateShippingOrder(orderPayload);
		
		assertTrue(commandEventProducer.eventEmitted);
		EventBase orderUpdatedEvent = commandEventProducer.getEventEmitted();
		assertNotNull(orderUpdatedEvent);
		assertTrue(OrderCommandEvent.UPDATED_ORDER_TYPE.equals(orderUpdatedEvent.getType()));
		OrderCommandEvent  orderCommand = (OrderCommandEvent)orderUpdatedEvent;
		assertTrue(orderPayload.getOrderID().equals(((OrderEventPayload)orderCommand.getPayload()).getOrderID()));
	}


	// ATTENTION this test just validate the mockup code as the update in the repository 
	// is done by the consumer mockup
	@Test
	@Order(3)
	public void shouldGetCreatedOrder() throws OrderCreationException {
		OrderEventPayload orderPayload = ShippingOrderTestDataFactory.given_a_new_order();
		service.createOrder(orderPayload);
		Optional<ShippingOrder> persistedOrder = service.getOrderByOrderID(orderPayload.getOrderID());
		assertTrue(persistedOrder.isPresent());
	}

	/**
	 * The following test is more complex in settings.
	 * The service will publish event so we use a command event producer mock
	 * The mock uses the repository as a mean to share data with consumer
	 * The Agent uses a command event consumer, which is also a mock
	 * For each command event received, handle the event so persist to the repository
	 * and then emit Order Events as facts
	 * BE SURE to add KAFKA_BROKERS as environment variable in the run configuration in eclipse.
	 * In maven the env is set in the pom.xml within unit test element
	 */
	@Test
	@Order(4)
	public void shouldGetOrderFromRepository() {
		// we need the command event consumer mock and event emitter with a real agent
		Properties properties = new Properties();
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		properties.put(ConsumerConfig.GROUP_ID_CONFIG,  "test-grp");
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.toString(false));
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "test-order-serv");

		KafkaConsumerMockup<String,String> kcm = new KafkaConsumerMockup<String,String>(properties,"order-commands");	
		OrderEventEmitterMock orderEventEmitter = new OrderEventEmitterMock();
		OrderEventEmitterMock errorEventEmitter = new OrderEventEmitterMock();
		// agent consume command events and generate order event
		KafkaInfrastructureConfig config = mock(KafkaInfrastructureConfig.class);
		when (config.getOrderCommandTopic()).thenReturn("order-command");
		OrderCommandAgent orderCommandAgent = new OrderCommandAgent(orderRepository,kcm,orderEventEmitter,errorEventEmitter,config);

		// need mockup emitter
		OrderCommandEventProducerMock eventEmitter = new OrderCommandEventProducerMock(orderRepository);
		// inject emitter and repo on the service to test
		ShippingOrderService service = new ShippingOrderService(eventEmitter,
				orderRepository);

		OrderEventPayload orderPayload = ShippingOrderTestDataFactory.given_a_new_order();
		try {
			service.createOrder(orderPayload);
		} catch (Exception e) {
			e.printStackTrace();
			Assertions.fail();
		}
		// well the repository was updated by the mockup command event emmitter, so let trick it by reset
		orderRepository.reset();

		// now mockup the event going to message broker
		kcm.setKey(orderPayload.getOrderID());
		String eventAsString = new Gson().toJson(eventEmitter.getEventEmitted());
		kcm.setValue(eventAsString);

		// the next 3 lines are simulating the runnable thread polling command events and processing them
		// List<OrderCommandEvent> results = orderCommandAgent.poll();
		// for (OrderCommandEvent event : results) {
        //    	orderCommandAgent.handle(event);
        // }
		// the handle persists in the repo... so following should work
		orderCommandAgent.poll();
		Optional<ShippingOrder> persistedOrder = service.getOrderByOrderID(orderPayload.getOrderID());
		Assertions.assertTrue(persistedOrder.isPresent());

	}

	private void assume_there_is_no_event_emitted(){
		assertFalse(commandEventProducer.eventEmitted);
	}
}
