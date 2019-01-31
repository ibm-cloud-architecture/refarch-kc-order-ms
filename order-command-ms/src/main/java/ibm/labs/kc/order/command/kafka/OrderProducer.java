package ibm.labs.kc.order.command.kafka;

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

import ibm.labs.kc.order.command.model.events.CreateOrderEvent;
import ibm.labs.kc.order.command.model.events.Event;
import ibm.labs.kc.order.command.model.events.EventEmitter;
import ibm.labs.kc.order.command.model.events.OrderEvent;
import ibm.labs.kc.order.command.model.events.UpdateOrderEvent;

public class OrderProducer implements EventEmitter {

    private static final Logger logger = LoggerFactory.getLogger(OrderProducer.class);

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
        String key;
        switch (orderEvent.getType()) {
        case OrderEvent.TYPE_CREATED:
            key = ((CreateOrderEvent)orderEvent).getPayload().getOrderID();
            break;
        case OrderEvent.TYPE_UPDATED:
            key = ((UpdateOrderEvent)orderEvent).getPayload().getOrderID();
            break;
        default:
            key = null;
        }
        String value = new Gson().toJson(orderEvent);
        ProducerRecord<String, String> record = new ProducerRecord<>(ApplicationConfig.ORDER_TOPIC, key, value);

        Future<RecordMetadata> send = kafkaProducer.send(record);
        send.get(ApplicationConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
    }

    @Override
    public void safeClose() {
        try {
            kafkaProducer.close();
        } catch (Exception e) {
            logger.warn("Failed closing Producer", e);
        }
    }

}
