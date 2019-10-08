package ut.orderms.infrastructure;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.kafka.KafkaInfrastructureConfig;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ut.KafkaConsumerMockup;
import ut.OrderEventEmitterMock;

/**
 * Test order command agent with consumer mock
 * @author jerome boyer
 *
 */
public class TestOrderCommandAgent {
	
	static OrderCommandAgent agent = null;
	static ShippingOrderRepository repository = null ;
	static KafkaConsumerMockup<String,String> orderCommandsConsumerMock = null;
	static EventEmitter orderEventProducerMock = null;
	@Before
	public void createAgent() {
		// use the mockup in this class. Do not create consumer multiple times
		if (orderCommandsConsumerMock == null) {
			orderCommandsConsumerMock = new KafkaConsumerMockup<String,String>(KafkaInfrastructureConfig.getConsumerProperties("test-grp","test-id",true,"earliest"),"orderCommands");	
		}
		if ( orderEventProducerMock == null) {
			orderEventProducerMock = new OrderEventEmitterMock();
		}
		repository = new ShippingOrderRepositoryMock();
		agent = new OrderCommandAgent(repository,orderCommandsConsumerMock,orderEventProducerMock);
	}
	/**
	 * Validate the agent consuming command event is receiving create order 
	 */
	@Test
	public void shouldReceiveACreateOrderCommandEvent() {
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
		Assert.assertTrue(OrderCommandEvent.TYPE_CREATE_ORDER.contentEquals(results.get(0).getType()));
		ShippingOrder shippingOrder = (ShippingOrder)results.get(0).getPayload();
		Assert.assertTrue("FreshCarrots".equals(shippingOrder.getProductID()));
		
	}
	
	@Test
	public void shouldPersistOrder() {
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
		assertNotNull(createOrderEvent);
		// should persist the order via the handler
		agent.handle(createOrderEvent);
		// verify order is persisted
		Optional<ShippingOrder> oso = repository.getOrderByOrderID("Order01");
		Assert.assertTrue(oso.isPresent());
	}
	
	@Test
	public void shouldUpdateOrder() {
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
		Optional<ShippingOrder> oso = repository.getOrderByOrderID("Order01");
		Assert.assertTrue(oso.isPresent());
		Assert.assertTrue(oso.get().getQuantity() == 30);
	}


}
