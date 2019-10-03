package ut.orderms.infrastructure;

import static org.junit.Assert.assertNotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.kafka.ApplicationConfig;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;

public class TestOrderCommandAgent {
	
	public class KafkaConsumerMockup<K,V> extends KafkaConsumer<K,V>  {
		 protected V value;
		 protected K key;
		 protected String topicName = "orderCommands";
		 protected int partitionNumber = 0;
		 protected int lastCommittedOffet = 0;
		 
		 public KafkaConsumerMockup(Properties properties) {
			super(properties);
		}
		 
		 @SuppressWarnings("unchecked")
		public void setValue(String v) {
			 this.value = (V)v;
		 }
		 
		@SuppressWarnings("unchecked")
		public void setKey(String k) {
			 this.key = (K)k;
		}

		@Override
		 public ConsumerRecords<K,V> poll(final Duration timeout) {
			List<ConsumerRecord<K,V>> l = new ArrayList<ConsumerRecord<K,V>>();
			ConsumerRecord<K,V> cs = new ConsumerRecord<K,V>(this.topicName, 
					this.partitionNumber, 
					this.lastCommittedOffet, 
					(K)this.key, (V)this.value);
			l.add(cs);
			TopicPartition tp = new TopicPartition("OrderCommands",0);
			Map<TopicPartition,List<ConsumerRecord<K,V>>> m = new HashMap<TopicPartition,List<ConsumerRecord<K,V>>>();
			m.put(tp, l);
			ConsumerRecords<K,V> records = new ConsumerRecords<K,V>(m);
			return records; 
		 }
	      
	}

	static OrderCommandAgent agent = null;
	static ShippingOrderRepository repository = null ;
	static KafkaConsumerMockup<String,String> kcm = null;
	
	@Before
	public void createAgent() {
		// use the mockup in this class. Do not create consumer multiple times
		if (kcm == null) {
			kcm = new KafkaConsumerMockup<String,String>(ApplicationConfig.getConsumerProperties("tests"));	
		}
		repository = new ShippingOrderRepositoryMock();
		agent = new OrderCommandAgent(repository,kcm);
	}
	/**
	 * Validate the agent consuming command event is receiving create order 
	 */
	@Test
	public void shouldReceiveACreateOrderCommandEvent() {
		kcm.setValue("{\"payload\":{\"orderID\":\"Order01\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":10,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"CreateOrderCommand\"}");
		kcm.setKey("Order01");
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
		kcm.setValue("{\"payload\":{\"orderID\":\"Order01\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":10,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"CreateOrderCommand\"}");
		kcm.setKey("Order01");
		List<OrderCommandEvent> results = agent.poll();
		OrderCommandEvent createOrderEvent = results.get(0);
		assertNotNull(createOrderEvent);
		// should persist the order via the handler
		agent.handle(createOrderEvent);
		// verify order is persisted
		Optional<ShippingOrder> oso = repository.getByID("Order01");
		Assert.assertTrue(oso.isPresent());
	}
	
	@Test
	public void shouldUpdateOrder() {
		kcm.setValue("{\"payload\":{\"orderID\":\"Order01\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":10,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"CreateOrderCommand\"}");
		kcm.setKey("Order01");
		List<OrderCommandEvent> results = agent.poll();
		OrderCommandEvent createOrderEvent = results.get(0);
		agent.handle(createOrderEvent);
		// create an update event
		kcm.setValue("{\"payload\":{\"orderID\":\"Order01\",\"productID\":\"FreshCarrots\""
				+ ",\"customerID\":\"Farm01\",\"quantity\":30,"
				+ "\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"pickupDate\":\"2019-01-14T17:48Z\","
				+ "\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},"
				+ "\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,"
				+ "\"type\":\"UpdateOrderCommand\"}");
		kcm.setKey("Order01");
		results = agent.poll();
		OrderCommandEvent updateOrderEvent = results.get(0);
		assertNotNull(updateOrderEvent);
		// should persist the order via the handler
		agent.handle(updateOrderEvent);
		// verify order is persisted
		Optional<ShippingOrder> oso = repository.getByID("Order01");
		Assert.assertTrue(oso.isPresent());
		Assert.assertTrue(oso.get().getQuantity() == 30);
	}


}
