package ut.orderms.infrastructure;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.events.OrderEvent;
import ibm.gse.orderms.infrastructure.events.OrderEventBase;
import ibm.gse.orderms.infrastructure.events.voyage.VoyageAssignedEvent;
import ibm.gse.orderms.infrastructure.events.voyage.VoyageAssignmentPayload;
import ibm.gse.orderms.infrastructure.kafka.KafkaInfrastructureConfig;
import ibm.gse.orderms.infrastructure.kafka.OrderEventAgent;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.KafkaConsumerMockup;
import ut.ShippingOrderTestDataFactory;

public class TestEventDeserialization {

	
	static OrderEventAgent agent;
	static ShippingOrderRepository repository = null ;
	static KafkaConsumerMockup<String,String> orderEventsConsumerMock = null;
	
	@Before
	public void createAgent() {
		// use the mockup in this class. Do not create consumer multiple times
		if (orderEventsConsumerMock == null) {
			orderEventsConsumerMock = new KafkaConsumerMockup<String,String>(KafkaInfrastructureConfig.getConsumerProperties("test-grp","test-id",true,"earliest"),"orders");	
		}
		repository = new ShippingOrderRepositoryMock();
		agent = new OrderEventAgent(orderEventsConsumerMock,repository);
	}
	
	@Test
	public void testOrderCreateEvent() {
		// prepare test data
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity(); 
		OrderEvent o = new OrderEvent(new Date().getTime(),
				OrderEventBase.TYPE_ORDER_CREATED,
				"1",order.toShippingOrderPayload());
		Gson gson = new Gson();
		String orderCreateAsString = gson.toJson(o);
		System.out.println(orderCreateAsString);
		
		
		OrderEventBase oea = agent.deserialize(orderCreateAsString);
		Assert.assertTrue(oea instanceof OrderEvent);
		OrderEvent oeOut = (OrderEvent) oea;
		Assert.assertTrue(oeOut.getPayload().getCustomerID().equals(order.getCustomerID()));
	}
	
	@Test
	public void testVoyageAssignedEvent() {
		// prepare test data
		
		VoyageAssignedEvent voyageAssignedEvent = new VoyageAssignedEvent(new Date().getTime(),
				"1",
				new VoyageAssignmentPayload("O01","V01")); 
		Gson gson = new Gson();
		String voyageAssignedEventAsString = gson.toJson(voyageAssignedEvent);
		System.out.println(voyageAssignedEventAsString);
		
		
		OrderEventBase oea = agent.deserialize(voyageAssignedEventAsString);
		Assert.assertTrue(oea instanceof VoyageAssignedEvent);
		VoyageAssignedEvent oeOut = (VoyageAssignedEvent) oea;
		Assert.assertTrue(oeOut.getPayload().getVoyageID().equals("V01"));
	}

}
