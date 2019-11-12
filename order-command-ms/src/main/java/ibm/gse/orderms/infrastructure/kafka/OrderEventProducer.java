package ibm.gse.orderms.infrastructure.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.events.OrderEvent;
import ibm.gse.orderms.infrastructure.events.OrderEventBase;
import ibm.gse.orderms.infrastructure.events.ShippingOrderPayload;

/**
 * Emits order events as fact about the shipping order. 
 */
public class OrderEventProducer implements EventEmitter {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);


    private KafkaProducer<String, String> kafkaProducer;
    private Properties properties;

    public OrderEventProducer() {  
    	initProducer();
    }

	private void initProducer() {
		properties = KafkaInfrastructureConfig.getProducerProperties("ordercmd-event-producer");
		properties.put(ProducerConfig.ACKS_CONFIG, "1");
		properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
	    kafkaProducer = new KafkaProducer<String, String>(properties);
	    logger.debug(properties.toString());
	}

    @Override
    @Retry(retryOn=TimeoutException.class,
    maxRetries = 4,
    maxDuration = 10000,
    delay = 200,
    jitter = 100,
    abortOn=InterruptedException.class)
    @Timeout(4000)
    public void emit(OrderEventBase event) throws InterruptedException, ExecutionException, TimeoutException {
        if (kafkaProducer == null) initProducer();
    	OrderEvent orderEvent = (OrderEvent)event;
        String key;
        switch (orderEvent.getType()) {
        case OrderEvent.TYPE_ORDER_CREATED:
        case OrderEvent.TYPE_ORDER_UPDATED:
            key = ((ShippingOrderPayload)orderEvent.getPayload()).getOrderID();
            break;
        default:
            key = null;
        }
        String value = new Gson().toJson(orderEvent);
        ProducerRecord<String, String> record = new ProducerRecord<>(KafkaInfrastructureConfig.getOrderTopic(), key, value);

        Future<RecordMetadata> send = kafkaProducer.send(record);
        send.get(KafkaInfrastructureConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
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
