package ut.orderms.infrastructure;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.events.OrderEventBase;
import ibm.gse.orderms.infrastructure.kafka.KafkaInfrastructureConfig;
import ibm.gse.orderms.infrastructure.kafka.OrderEventAgent;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.KafkaConsumerMockup;
import ut.ShippingOrderTestDataFactory;

public class TestOrderEventAgent {

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
	/**
	 * Send order created events ...
	 */
	public void shouldAllocateAVoyageIDToOrder() {
		// prepare data for the test
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		repository.addNewShippingOrder(order);
		String voyageEvent = "{\"timestamp\": " + new Date().getTime() 
		    		+ ",\"type\": \"VoyageAssigned\", \"version\": \"1\"," 
		    		+ " \"payload\": { \"voyageID\": \"V101\",\"orderID\": \"" + order.getOrderID()
		    		+ "\"}}";
		orderEventsConsumerMock.setValue(voyageEvent);
		orderEventsConsumerMock.setKey(order.getOrderID());
		
		List<OrderEventBase> events = agent.poll();
		for (OrderEventBase event : events) {
			agent.handle(event);
		}
		Optional<ShippingOrder> orderOption = repository.getOrderByOrderID(order.getOrderID());
		Assert.assertTrue("V101".equals(orderOption.get().getVoyageID()));	
	}
	
	@Test
	/**
	 * Send order created events ...
	 */
	public void shouldAllocateAReeferIDToOrder() {
		// prepare data for the test
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		repository.addNewShippingOrder(order);
		String voyageEvent = "{\"timestamp\": " + new Date().getTime() 
		    		+ ",\"type\": \"ReeferAssigned\", \"version\": \"1\"," 
		    		+ " \"payload\": { \"containerID\": \"C01\",\"orderID\": \"" + order.getOrderID()
		    		+ "\"}}";
		orderEventsConsumerMock.setValue(voyageEvent);
		orderEventsConsumerMock.setKey(order.getOrderID());
		
		List<OrderEventBase> events = agent.poll();
		for (OrderEventBase event : events) {
			agent.handle(event);
		}
		Optional<ShippingOrder> orderOption = repository.getOrderByOrderID(order.getOrderID());
		Assert.assertTrue("C01".equals(orderOption.get().getReeferID()));	
	}

}
