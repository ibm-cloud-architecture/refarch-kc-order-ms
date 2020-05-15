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


public class ErrorEventProducer implements EventEmitterTransactional {

    private static final Logger logger = LoggerFactory.getLogger(ErrorEventProducer.class);

    private KafkaProducer<String, String> kafkaProducer;
    private Properties properties;
    private KafkaInfrastructureConfig config;

    public ErrorEventProducer() {
        config = new KafkaInfrastructureConfig();
        initProducer();
    }

    private void initProducer() {
		properties = KafkaInfrastructureConfig.getProducerProperties("error-event-producer");
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "error-1");
	    kafkaProducer = new KafkaProducer<String, String>(properties);
        logger.debug(properties.toString());
        // registers the producer with the broker as one that can use transactions, 
        // identifying it by its transactional.id and a sequence number
        kafkaProducer.initTransactions();
	}

    // public ErrorEventProducer() {
    //     Properties properties = KafkaInfrastructureConfig.getProducerProperties("error-event-producer");
    //     kafkaProducer = new KafkaProducer<String, String>(properties);
    // }

    @Override
    public void emit(EventBase event) throws InterruptedException, ExecutionException, TimeoutException {
        logger.error("[ERROR] - The emit method in the ErrorEventProducer class has been called for the event: " + event.toString());
        logger.error("[ERROR] - This producer is TRANSACTIONAL. Please, check the code and use the transactional method");
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

        ErrorEvent errorEvent = (ErrorEvent) event;
        String value = new Gson().toJson(errorEvent);

        ProducerRecord<String, String> record = new ProducerRecord<>(config.getErrorTopic(), value);

        kafkaProducer.beginTransaction();
        Future<RecordMetadata> send = kafkaProducer.send(record);
        send.get(KafkaInfrastructureConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
        /**
         * Here is where the consume-transform-produce loop pattern magic happens.
         * Kafka transactions allow us to commit the offsets read from the consumer as part of the transaction.
         * That way, both the events produced and the offsets for the records consumed are either both committed or none.
         * As a result, we ensure no command from the order-commands topic is committed, therefore treated as processed,
         * unless we produce the resulting event into the order topics, which is the latest of the actions we must complete
         * for any given command for this microservice. In this case, the event we are producing is an error for the command read.
         **/
        kafkaProducer.sendOffsetsToTransaction(offsetToCommit, groupID);
        kafkaProducer.commitTransaction();
    }

    @Override
    public void safeClose() {
        try {
            kafkaProducer.close();
        } catch (Exception e) {
            logger.warn("Failed to close Producer", e);
        }
    }

}
