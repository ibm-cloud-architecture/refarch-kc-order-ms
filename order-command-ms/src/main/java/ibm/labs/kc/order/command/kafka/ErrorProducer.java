package ibm.labs.kc.order.command.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.google.gson.Gson;

import ibm.labs.kc.order.command.model.events.ErrorEvent;
import ibm.labs.kc.order.command.model.events.Event;
import ibm.labs.kc.order.command.model.events.EventEmitter;

public class ErrorProducer implements EventEmitter {

    private static final Logger logger = Logger.getLogger(ErrorProducer.class.getName());

    private KafkaProducer<String, String> kafkaProducer;

    public ErrorProducer() {
        Properties properties = ApplicationConfig.getProducerProperties("error-query-producer");
        kafkaProducer = new KafkaProducer<String, String>(properties);
    }

    @Override
    public void emit(Event event) throws InterruptedException, ExecutionException, TimeoutException {
        ErrorEvent errorEvent = (ErrorEvent) event;
        String value = new Gson().toJson(errorEvent);

        ProducerRecord<String, String> record = new ProducerRecord<>(ApplicationConfig.ERROR_TOPIC, value);

        Future<RecordMetadata> send = kafkaProducer.send(record);
        send.get(ApplicationConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
    }

    @Override
    public void safeClose() {
        try {
            kafkaProducer.close(ApplicationConfig.PRODUCER_CLOSE_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warning("Failed to close Producer");
        }
    }

}
