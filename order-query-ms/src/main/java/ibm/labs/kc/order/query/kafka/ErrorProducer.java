package ibm.labs.kc.order.query.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.google.gson.Gson;

import ibm.labs.kc.order.query.model.events.ErrorEvent;
import ibm.labs.kc.order.query.model.events.Event;
import ibm.labs.kc.order.query.model.events.EventEmitter;

public class ErrorProducer implements EventEmitter {

    private KafkaProducer<String, String> kafkaProducer;

    public ErrorProducer() {
        Properties properties = ApplicationConfig.getProducerProperties("error-query-producer");
        kafkaProducer = new KafkaProducer<String, String>(properties);
    }

    @Override
    public void emit(Event event) throws InterruptedException, ExecutionException, TimeoutException {
        ErrorEvent errorEvent = (ErrorEvent)event;
        String value = new Gson().toJson(errorEvent);

        ProducerRecord<String, String> record = new ProducerRecord<>(ApplicationConfig.ERROR_TOPIC, value);

        Future<RecordMetadata> send = kafkaProducer.send(record);
        send.get(ApplicationConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
    }

}
