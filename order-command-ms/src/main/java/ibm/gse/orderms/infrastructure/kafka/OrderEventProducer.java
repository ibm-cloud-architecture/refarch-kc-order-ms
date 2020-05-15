package ibm.gse.orderms.infrastructure.kafka;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ibm.gse.orderms.infrastructure.events.EventEmitterTransactional;
import ibm.gse.orderms.infrastructure.events.EventBase;
import ibm.gse.orderms.infrastructure.events.order.OrderEvent;
import ibm.gse.orderms.infrastructure.events.order.OrderRejectEvent;
import ibm.gse.orderms.infrastructure.events.order.OrderCancelledEvent;
import ibm.gse.orderms.infrastructure.events.order.OrderEventPayload;
import ibm.gse.orderms.infrastructure.events.order.OrderCancelAndRejectPayload;

/**
 * Emits order events as fact about the shipping order. 
 */
public class OrderEventProducer implements EventEmitterTransactional {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);


    private KafkaProducer<String, String> kafkaProducer;
    private Properties properties;
    private KafkaInfrastructureConfig config;

    public OrderEventProducer() {  
        initProducer();
    }
    
	private void initProducer() {
        config = new KafkaInfrastructureConfig();
		properties = KafkaInfrastructureConfig.getProducerProperties("ordercmd-event-producer");
		// properties.put(ProducerConfig.ACKS_CONFIG, "1");
        // properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "order-1");
	    kafkaProducer = new KafkaProducer<String, String>(properties);
        logger.debug(properties.toString());
        // registers the producer with the broker as one that can use transactions, 
        // identifying it by its transactional.id and a sequence number
        kafkaProducer.initTransactions();
	}

    @Override
    @Retry(retryOn=TimeoutException.class,
    maxRetries = 4,
    maxDuration = 10000,
    delay = 200,
    jitter = 100,
    abortOn=InterruptedException.class)
    @Timeout(4000)
    public void emit(EventBase event) throws InterruptedException, ExecutionException, TimeoutException {
        if (kafkaProducer == null) initProducer();
        String key;
        String value;
        switch (event.getType()) {
        case OrderEvent.TYPE_ORDER_CREATED:
        case OrderEvent.TYPE_ORDER_UPDATED:
            OrderEvent orderEvent = (OrderEvent)event;
            key = ((OrderEventPayload)orderEvent.getPayload()).getOrderID();
            value = new Gson().toJson(orderEvent);
            break;
        case OrderEvent.TYPE_ORDER_REJECTED:
            OrderRejectEvent orderRejected = (OrderRejectEvent) event;
            key = ((OrderCancelAndRejectPayload)orderRejected.getPayload()).getOrderID();
            value = new Gson().toJson(orderRejected);
            break;
        case OrderEvent.TYPE_ORDER_CANCELLED:
            OrderCancelledEvent orderCancelled = (OrderCancelledEvent) event;
            key = ((OrderCancelAndRejectPayload)orderCancelled.getPayload()).getOrderID();
            value = new Gson().toJson(orderCancelled);
            break;
        default:
            key = null;
            value = null;
        }
        ProducerRecord<String, String> record = new ProducerRecord<>(config.getOrderTopic(), key, value);

        kafkaProducer.beginTransaction();
        Future<RecordMetadata> send = kafkaProducer.send(record);
        send.get(KafkaInfrastructureConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
        kafkaProducer.commitTransaction();
    }

    @Override
    @Retry(retryOn=TimeoutException.class,
    maxRetries = 4,
    maxDuration = 10000,
    delay = 200,
    jitter = 100,
    abortOn=InterruptedException.class)
    @Timeout(4000)
    public void emitWithOffsets(EventBase event, Map<TopicPartition, OffsetAndMetadata> offsetToCommit, String groupID) throws InterruptedException, ExecutionException, TimeoutException {
        if (kafkaProducer == null) initProducer();
        String key;
        String value;
        switch (event.getType()) {
        case OrderEvent.TYPE_ORDER_CREATED:
        case OrderEvent.TYPE_ORDER_UPDATED:
            OrderEvent orderEvent = (OrderEvent)event;
            key = ((OrderEventPayload)orderEvent.getPayload()).getOrderID();
            value = new Gson().toJson(orderEvent);
            break;
        case OrderEvent.TYPE_ORDER_REJECTED:
            OrderRejectEvent orderRejected = (OrderRejectEvent) event;
            key = ((OrderCancelAndRejectPayload)orderRejected.getPayload()).getOrderID();
            value = new Gson().toJson(orderRejected);
            break;
        case OrderEvent.TYPE_ORDER_CANCELLED:
            OrderCancelledEvent orderCancelled = (OrderCancelledEvent) event;
            key = ((OrderCancelAndRejectPayload)orderCancelled.getPayload()).getOrderID();
            value = new Gson().toJson(orderCancelled);
            break;
        default:
            key = null;
            value = null;
        }
        ProducerRecord<String, String> record = new ProducerRecord<>(config.getOrderTopic(), key, value);

        kafkaProducer.beginTransaction();
        Future<RecordMetadata> send = kafkaProducer.send(record);
        send.get(KafkaInfrastructureConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
        /**
         * Here is where the consume-transform-produce loop pattern magic happens.
         * Kafka transactions allow us to commit the offsets read from the consumer as part of the transaction.
         * That way, both the events produced and the offsets for the records consumed are either both committed or none.
         * As a result, we ensure no command from the order-commands topic is committed, therefore treated as processed,
         * unless we produce the resulting event into the order topics, which is the latest of the actions we must complete
         * for any given command for this microservice. In this case, it will be an OrderCreated, OrderUpdated or OrderRejected event.
         **/
        kafkaProducer.sendOffsetsToTransaction(offsetToCommit, groupID);
        kafkaProducer.commitTransaction();
    }

    @Override
    public void safeClose() {
        try {
        	kafkaProducer.flush();
            kafkaProducer.close();
        } catch (Exception e) {
            logger.warn("Failed closing Producer", e);
        }
    }

}
