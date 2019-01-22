package ibm.labs.kc.order.command.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.google.gson.Gson;

import ibm.labs.kc.order.command.model.events.Event;
import ibm.labs.kc.order.command.model.events.EventEmitter;
import ibm.labs.kc.order.command.model.events.OrderEvent;

public class OrderProducer implements EventEmitter {

    private static OrderProducer instance;
    private KafkaProducer<String, String> kafkaProducer;

    public synchronized static EventEmitter instance() {
        if (instance == null) {
            instance = new OrderProducer();
        }
        return instance;
    }

    public OrderProducer() {
        Properties properties = ApplicationConfig.getProducerProperties("order-command-producer");
        kafkaProducer = new KafkaProducer<String, String>(properties);
    }

    @Override
    public void emit(Event event) throws InterruptedException, ExecutionException, TimeoutException {
        OrderEvent orderEvent = (OrderEvent)event;
        String value = new Gson().toJson(orderEvent);
        String key = orderEvent.getPayload().getOrderID();
        ProducerRecord<String, String> record = new ProducerRecord<>(ApplicationConfig.ORDER_TOPIC, key, value);

        Future<RecordMetadata> send = kafkaProducer.send(record);
        send.get(ApplicationConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
    }

}
