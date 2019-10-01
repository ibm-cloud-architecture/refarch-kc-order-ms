package ut.orderms.infrastructure;

import static org.junit.Assert.assertNotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Test;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.kafka.ApplicationConfig;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.repository.OrderRepositoryMock;

public class TestOrderCommandAgent {
	
	public class KafkaConsumerMockup<K,V> extends KafkaConsumer<K,V>  {
		
		 public KafkaConsumerMockup(Properties properties) {
			super(properties);
		}

		@Override
		 public ConsumerRecords<K,V> poll(final Duration timeout) {
			List<ConsumerRecord<K,V>> l = new ArrayList<ConsumerRecord<K,V>>();
			String value = "{\"payload\":{\"orderID\":\"Order01\",\"productID\":\"FreshCarrots\",\"customerID\":\"Farm01\",\"quantity\":10,\"pickupAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},\"pickupDate\":\"2019-01-14T17:48Z\",\"destinationAddress\":{\"street\":\"Street\",\"city\":\"City\",\"country\":\"County\",\"state\":\"State\",\"zipcode\":\"Zipcode\"},\"expectedDeliveryDate\":\"2019-03-15T17:48Z\",\"status\":\"pending\"},\"timestampMillis\":0,\"type\":\"CreateOrderCommand\"}";
			ConsumerRecord<K,V> cs = new ConsumerRecord<K,V>("orderCommands", 0, 1, (K)"Order01", (V)value);
			l.add(cs);
			TopicPartition tp = new TopicPartition("OrderCommands",0);
			Map<TopicPartition,List<ConsumerRecord<K,V>>> m = new HashMap<TopicPartition,List<ConsumerRecord<K,V>>>();
			m.put(tp, l);
			ConsumerRecords<K,V> records = new ConsumerRecords<K,V>(m);
			return records; 
		 }
	      
	}

	@Test
	public void shouldReceiveACommandEvent() {
		KafkaConsumerMockup kcm = new KafkaConsumerMockup(ApplicationConfig.getConsumerProperties("tests"));
		OrderCommandAgent agent = new OrderCommandAgent(new OrderRepositoryMock(),kcm);
		List<OrderCommandEvent> results = agent.poll();
		assertNotNull(results);
		assertNotNull(results.get(0));
		assertNotNull(results.get(0).getPayload());
		Assert.assertTrue(OrderCommandEvent.TYPE_CREATE_ORDER.contentEquals(results.get(0).getType()));
		ShippingOrder shippingOrder = (ShippingOrder)results.get(0).getPayload();
		Assert.assertTrue("FreshCarrots".equals(shippingOrder.getProductID()));
	}

}
