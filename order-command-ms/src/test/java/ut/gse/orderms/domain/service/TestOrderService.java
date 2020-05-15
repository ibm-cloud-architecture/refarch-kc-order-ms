package ut.gse.orderms.domain.service;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.domain.service.ShippingOrderService;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.EventBase;
import ibm.gse.orderms.infrastructure.events.order.OrderEventPayload;
import ibm.gse.orderms.infrastructure.kafka.KafkaInfrastructureConfig;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.repository.OrderUpdateException;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.KafkaConsumerMockup;
import ut.OrderCommandEventProducerMock;
import ut.OrderEventEmitterMock;
import ut.ShippingOrderTestDataFactory;

import static org.mockito.Mockito.*; 

public class TestOrderService {

	public static ShippingOrderRepository orderRepository = new ShippingOrderRepositoryMock();


	@Test
	public void shouldEmitEventOnOrderCreation() {
		OrderCommandEventProducerMock commandEventProducer = new OrderCommandEventProducerMock(orderRepository);

		ShippingOrderService service = new ShippingOrderService(commandEventProducer,
				orderRepository);
		Assert.assertFalse(commandEventProducer.eventEmitted);

		OrderEventPayload orderPayload = ShippingOrderTestDataFactory.orderPayloadFixture();
		try {
			service.createOrder(orderPayload);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		Assert.assertTrue(commandEventProducer.eventEmitted);
		EventBase createOrderEvent = commandEventProducer.getEventEmitted();

		Assert.assertNotNull(createOrderEvent);
		Assert.assertTrue(OrderCommandEvent.TYPE_CREATE_ORDER.equals(createOrderEvent.getType()));
		OrderCommandEvent  orderCommand = (OrderCommandEvent)createOrderEvent;
		Assert.assertTrue(orderPayload.getOrderID().equals(((OrderEventPayload)orderCommand.getPayload()).getOrderID()));

	}

	@Test
	public void shouldEmitEventOnOrderUpdate() {
		OrderCommandEventProducerMock commandEventProducer = new OrderCommandEventProducerMock(orderRepository);
		ShippingOrderService service = new ShippingOrderService(commandEventProducer,
				orderRepository);

		OrderEventPayload orderPayload = ShippingOrderTestDataFactory.orderPayloadFixture();
		try {
			service.createOrder(orderPayload);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
		orderPayload.setQuantity(100);
		try {
			service.updateShippingOrder(orderPayload);
		} catch (OrderUpdateException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertTrue(commandEventProducer.eventEmitted);
		EventBase orderUpdatedEvent = commandEventProducer.getEventEmitted();
		Assert.assertNotNull(orderUpdatedEvent);
		Assert.assertTrue(OrderCommandEvent.TYPE_UPDATE_ORDER.equals(orderUpdatedEvent.getType()));
		OrderCommandEvent  orderCommand = (OrderCommandEvent)orderUpdatedEvent;
		Assert.assertTrue(orderPayload.getOrderID().equals(((OrderEventPayload)orderCommand.getPayload()).getOrderID()));
	}


	// ATTENTION this test just validates the mockup as the update in the repository is done by the agent
	@Test
	public void shouldGetCreatedOrder() {
		OrderCommandEventProducerMock commandEventProducer = new OrderCommandEventProducerMock(orderRepository);
		ShippingOrderService service = new ShippingOrderService(commandEventProducer,
				orderRepository);

		OrderEventPayload orderPayload = ShippingOrderTestDataFactory.orderPayloadFixture();
		try {
			service.createOrder(orderPayload);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
		Optional<ShippingOrder> persistedOrder = service.getOrderByOrderID(orderPayload.getOrderID());
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
		Properties properties = new Properties();
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		properties.put(ConsumerConfig.GROUP_ID_CONFIG,  "test-grp");
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.toString(false));
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "test-clientID");

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

		OrderEventPayload orderPayload = ShippingOrderTestDataFactory.orderPayloadFixture();
		try {
			service.createOrder(orderPayload);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
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
		Assert.assertTrue(persistedOrder.isPresent());

	}
}
