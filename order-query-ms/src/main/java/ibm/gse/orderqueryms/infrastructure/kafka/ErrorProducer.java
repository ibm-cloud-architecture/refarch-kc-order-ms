package ibm.gse.orderqueryms.infrastructure.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ibm.gse.orderqueryms.infrastructure.events.Event;
import ibm.gse.orderqueryms.infrastructure.events.EventEmitter;
import ibm.gse.orderqueryms.infrastructure.events.error.ErrorEvent;

public class ErrorProducer implements EventEmitter {

    private static final Logger logger = LoggerFactory.getLogger(ErrorProducer.class.getName());

    private KafkaProducer<String, String> kafkaProducer;

    public ErrorProducer() {
        Properties properties = ApplicationConfig.getProducerProperties("error-query-producer");
        kafkaProducer = new KafkaProducer<String, String>(properties);
    }

    @Override
    public void emit(Event event) throws InterruptedException, ExecutionException, TimeoutException {
        ErrorEvent errorEvent = (ErrorEvent) event;
        String value = new Gson().toJson(errorEvent);

        ProducerRecord<String, String> record = new ProducerRecord<>(ApplicationConfig.getErrorTopic(), value);

        Future<RecordMetadata> send = kafkaProducer.send(record);
        send.get(ApplicationConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
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
