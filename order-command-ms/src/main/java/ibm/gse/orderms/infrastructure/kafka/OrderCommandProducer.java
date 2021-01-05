package ibm.gse.orderms.infrastructure.kafka;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderms.domain.events.EventBase;
import ibm.gse.orderms.domain.events.EventEmitter;
import ibm.gse.orderms.domain.events.command.OrderCommandEvent;
import ibm.gse.orderms.domain.events.order.OrderEventPayload;

public class OrderCommandProducer implements EventEmitter  {
	private static final Logger logger = LoggerFactory.getLogger(OrderCommandProducer.class);
	
	private KafkaProducer<String, String> kafkaProducer;
	private Properties properties;
	
    public OrderCommandProducer() {
		initProducer();
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
	public void emit(EventBase event) throws Exception {
    	if (kafkaProducer == null) initProducer();
		OrderCommandEvent orderCommandEvent = (OrderCommandEvent)event;
        String key = ((OrderEventPayload)orderCommandEvent.getPayload()).getOrderID();
        String value = new Gson().toJson(orderCommandEvent);
        
        try {
	        ProducerRecord<String, String> record = new ProducerRecord<>(KafkaInfrastructureConfig.ORDER_COMMAND_TOPIC, key, value);
			Future<RecordMetadata> send = kafkaProducer.send(record);
	        logger.info("Command event sent: " + value);
	        send.get(KafkaInfrastructureConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
        } catch (KafkaException e){
        	logger.error(e.getMessage());
        	throw new KafkaException(e);
        }
	}

	@Override
	public void safeClose() {
		try {
			kafkaProducer.flush();
            kafkaProducer.close();
        } catch (Exception e) {
            logger.error("Failed closing Producer", e);
        }
		
	}

	private void initProducer() {
    	properties = KafkaInfrastructureConfig.getProducerProperties("ordercmd-command-producer");
		properties.put(ProducerConfig.ACKS_CONFIG, "all");
		properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
	    kafkaProducer = new KafkaProducer<String, String>(properties);
	    logger.debug(properties.toString());
    }
}
