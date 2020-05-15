package ut.orderms.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.events.EventBase;
import ibm.gse.orderms.infrastructure.events.order.OrderEvent;
import ibm.gse.orderms.infrastructure.events.order.OrderEventPayload;
import ibm.gse.orderms.infrastructure.kafka.OrderEventAgent;
import ibm.gse.orderms.infrastructure.repository.OrderCreationException;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.KafkaConsumerMockup;
import ut.ShippingOrderTestDataFactory;

/**
 * Order event agent subscribes to orders topic. 
 * Test all possible events it can get from external producers in this class
 * 
 * For unit testing we use the repository as a way to share the data between 
 * the consumer mockup
 * @author jerome boyer
 *
 */
public class TestOrderEventAgent {

	static OrderEventAgent agent;
	static ShippingOrderRepository repository = null ;
	static KafkaConsumerMockup<String,String> orderEventsConsumerMock = null;
	static Gson parser = new Gson();
	
	@Before
	public void createAgent() {
		// use the mockup in this class. Do not create consumer multiple times
		if (orderEventsConsumerMock == null) {
			Properties properties = ShippingOrderTestDataFactory.buildConsumerKafkaProperties();
			orderEventsConsumerMock = new KafkaConsumerMockup<String,String>(properties,"orders");	
		}
		repository = new ShippingOrderRepositoryMock();
		agent = new OrderEventAgent(orderEventsConsumerMock,repository);
	}
	
	
	@Test
	/**
	 * Test context: The create command event processing has persisted  the order into the repository
	 * and emitted an order create event. As the consumer will get this message it will
	 * do nothing in this microservice.
	 * 
	 * This function simulates sending the OrderCreated event.
	 */
	public void shouldDoNothingOnOrderCreatedEvent() {
		OrderEventPayload order = ShippingOrderTestDataFactory.orderPayloadFixture();
		OrderEvent orderCreatedEvent = new OrderEvent(); 
		orderCreatedEvent.setType(EventBase.TYPE_ORDER_CREATED);
		orderEventsConsumerMock.setValue(parser.toJson(orderCreatedEvent));
		orderEventsConsumerMock.setKey(order.getOrderID());
		List<EventBase> events = agent.poll();
		EventBase orderCreatedEventReceived = events.get(0);
		Assert.assertNotNull(orderCreatedEventReceived);
		for (EventBase event : events) {
			agent.handle(event);
		}
	}
	
	@Test
	public void shouldDoNothingOnOrderUpdatedEvent() {
		OrderEventPayload order = ShippingOrderTestDataFactory.orderPayloadFixture();
		OrderEvent orderCreatedEvent = new OrderEvent(); 
		orderCreatedEvent.setType(EventBase.TYPE_ORDER_UPDATED);
		orderEventsConsumerMock.setValue(parser.toJson(orderCreatedEvent));
		orderEventsConsumerMock.setKey(order.getOrderID());
		List<EventBase> events = agent.poll();
		EventBase orderUpdatedEventReceived = events.get(0);
		Assert.assertNotNull(orderUpdatedEventReceived);
		for (EventBase event : events) {
			agent.handle(event);
		}
	}
	
	@Test
	/**
	 * When receiving a voyage assignment event, the voyage id is added to the
	 * matching order in the repository.
	 */
	public void shouldAllocateAVoyageIDToOrder() throws OrderCreationException {
		// The order is in the repository.
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		repository.addOrUpdateNewShippingOrder(order);
		// mockup a voyage assignment event
		String voyageEvent = "{\"timestamp\": " + new Date().getTime() 
		    		+ ",\"type\": \"VoyageAssigned\", \"version\": \"1\"," 
		    		+ " \"payload\": { \"voyageID\": \"V101\",\"orderID\": \"" + order.getOrderID()
		    		+ "\"}}";
		orderEventsConsumerMock.setValue(voyageEvent);
		orderEventsConsumerMock.setKey(order.getOrderID());
		
		List<EventBase> events = agent.poll();
		for (EventBase event : events) {
			agent.handle(event);
		}
		// verify the repository is update with the voyage id
		Optional<ShippingOrder> orderOption = repository.getOrderByOrderID(order.getOrderID());
		Assert.assertTrue("V101".equals(orderOption.get().getVoyageID()));	
	}
	
	@Test
	/**
	 * Send order created events ...
	 */
	public void shouldAllocateAContainerIDToOrder() throws OrderCreationException {
		// prepare data for the test
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		repository.addOrUpdateNewShippingOrder(order);
		String voyageEvent = "{\"timestamp\": " + new Date().getTime() 
		    		+ ",\"type\": \"ContainerAllocated\", \"version\": \"1\"," 
		    		+ " \"payload\": { \"containerID\": \"C01\",\"orderID\": \"" + order.getOrderID()
		    		+ "\"}}";
		orderEventsConsumerMock.setValue(voyageEvent);
		orderEventsConsumerMock.setKey(order.getOrderID());
		
		List<EventBase> events = agent.poll();
		for (EventBase event : events) {
			agent.handle(event);
		}
		Optional<ShippingOrder> orderOption = repository.getOrderByOrderID(order.getOrderID());
		assertTrue("Order not found in repository", orderOption.isPresent());
		assertEquals("C01",orderOption.get().getContainerID());	
	}

}
