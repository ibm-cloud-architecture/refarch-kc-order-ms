package ut.orderms.infrastructure;

import java.util.Date;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.events.EventBase;
import ibm.gse.orderms.infrastructure.events.order.OrderCancelledEvent;
import ibm.gse.orderms.infrastructure.events.order.OrderEvent;
import ibm.gse.orderms.infrastructure.events.container.ContainerAllocatedEvent;
import ibm.gse.orderms.infrastructure.events.container.ContainerAllocatedPayload;
import ibm.gse.orderms.infrastructure.events.voyage.VoyageAssignedEvent;
import ibm.gse.orderms.infrastructure.events.voyage.VoyageAssignmentPayload;
import ibm.gse.orderms.infrastructure.kafka.OrderEventAgent;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.KafkaConsumerMockup;
import ut.ShippingOrderTestDataFactory;

public class TestEventDeserialization {

	
	static OrderEventAgent agent;
	static ShippingOrderRepository repository = null ;
	static KafkaConsumerMockup<String,String> orderEventsConsumerMock = null;
	static Gson gson = new Gson();
	
	@BeforeClass
	public static void createAgent() {
		Properties properties = ShippingOrderTestDataFactory.buildConsumerKafkaProperties();
		orderEventsConsumerMock = new KafkaConsumerMockup<String,String>(properties,"orders");	
		repository = new ShippingOrderRepositoryMock();
		agent = new OrderEventAgent(orderEventsConsumerMock,repository);
	}
	
	@Test
	public void testOrderCreateEvent() {
		// prepare test data
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity(); 
		OrderEvent o = new OrderEvent(new Date().getTime(),
				EventBase.TYPE_ORDER_CREATED,
				"1",order.toShippingOrderPayload());
		String orderCreateAsString = gson.toJson(o);
		EventBase oea = agent.deserialize(orderCreateAsString);
		Assert.assertTrue(oea instanceof OrderEvent);
		OrderEvent oeOut = (OrderEvent) oea;
		Assert.assertTrue(oeOut.getPayload().getCustomerID().equals(order.getCustomerID()));
	}
	
	@Test
	public void shouldDeserializeVoyageAssignedEvent() {
		// prepare test data
		
		ContainerAllocatedEvent containerAssignedEvent = new ContainerAllocatedEvent(new Date().getTime(),
				"1",
				"O01",
				new ContainerAllocatedPayload("O01","C01")); 
		String containerAssignedEventAsString = gson.toJson(containerAssignedEvent);

		EventBase oea = agent.deserialize(containerAssignedEventAsString);
		Assert.assertTrue(oea instanceof ContainerAllocatedEvent);
		ContainerAllocatedEvent oeOut = (ContainerAllocatedEvent) oea;
		Assert.assertTrue(oeOut.getPayload().getContainerID().equals("C01"));
	}
	
	@Test
	public void shouldDeserializeContainerAllocatedEvent() {
		// prepare test data
		
		VoyageAssignedEvent voyageAssignedEvent = new VoyageAssignedEvent(new Date().getTime(),
				"1",
				new VoyageAssignmentPayload("O01","V01")); 
		
		String voyageAssignedEventAsString = gson.toJson(voyageAssignedEvent);
		System.out.println(voyageAssignedEventAsString);
		
		
		EventBase oea = agent.deserialize(voyageAssignedEventAsString);
		Assert.assertTrue(oea instanceof VoyageAssignedEvent);
		VoyageAssignedEvent oeOut = (VoyageAssignedEvent) oea;
		Assert.assertTrue(oeOut.getPayload().getVoyageID().equals("V01"));
	}

	@Test
	public void shouldDeserializeOrderCancelledEvent() {
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		String orderID = order.getOrderID(); 
		OrderCancelledEvent event = new OrderCancelledEvent(new Date().getTime(),
				"1", order.toOrderCancelAndRejectPayload("testing deserialize"));
		String eventAsStr = gson.toJson(event);
		EventBase oea = agent.deserialize(eventAsStr);
		Assert.assertTrue(oea instanceof OrderCancelledEvent);
		OrderCancelledEvent oeOut = (OrderCancelledEvent) oea;
		Assert.assertTrue(oeOut.getPayload().getOrderID().equals(orderID));
	}
}
