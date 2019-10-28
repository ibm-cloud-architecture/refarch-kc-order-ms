package ibm.gse.orderms.infrastructure.kafka;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.events.OrderEventBase;

public class OrderCommandProducer implements EventEmitter  {
	private static final Logger logger = LoggerFactory.getLogger(OrderCommandProducer.class);
	
	private KafkaProducer<String, String> kafkaProducer;
	
    
    public OrderCommandProducer() {
    	Properties properties = KafkaInfrastructureConfig.getProducerProperties("ordercmd-command-producer");
		properties.put(ProducerConfig.ACKS_CONFIG, "all");
		properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "order-cmd-1");
	    kafkaProducer = new KafkaProducer<String, String>(properties);
	    logger.debug(properties.toString());
        // registers the producer with the broker as one that can use transactions, 
        // identifying it by its transactional.id and a sequence number
        kafkaProducer.initTransactions();
    }
    
	/**
	 * produce exactly one command and ensure all brokers have acknowledged
	 * the command (the replication is done)
	 * 
	 */
    @Override
    @Retry(retryOn=TimeoutException.class,
    maxRetries = 4,
    maxDuration = 10000,
    delay = 200,
    jitter = 100,
    abortOn=InterruptedException.class)
    @Timeout(4000)
	public void emit(OrderEventBase event) throws Exception {

		OrderCommandEvent orderCommandEvent = (OrderCommandEvent)event;
        String key = ((ShippingOrder)orderCommandEvent.getPayload()).getOrderID();
        String value = new Gson().toJson(orderCommandEvent);
        
        try {
	        kafkaProducer.beginTransaction();
	        ProducerRecord<String, String> record = new ProducerRecord<>(KafkaInfrastructureConfig.getOrderCommandTopic(), key, value);
	        Future<RecordMetadata> send = kafkaProducer.send(record);
	        logger.info("Command event sent: " + value);
	        send.get(KafkaInfrastructureConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
	        kafkaProducer.commitTransaction();
        } catch (KafkaException e){
        	kafkaProducer.abortTransaction();
        	logger.error(e.getMessage());
        	throw new KafkaException(e);
        }
	}

	@Override
	public void safeClose() {
		try {
            kafkaProducer.close();
        } catch (Exception e) {
            logger.error("Failed closing Producer", e);
        }
		
	}

}
