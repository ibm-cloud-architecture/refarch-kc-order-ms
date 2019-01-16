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

import ibm.labs.kc.order.query.model.Order;

public class OrderConsumer {
    
    private static OrderConsumer instance;
    private KafkaProducer<String, String> kafkaProducer;
    
    public synchronized static OrderConsumer instance() {
        if (instance == null) {
            instance = new OrderConsumer();
        }
        return instance;
    }

    public OrderConsumer() {
        Properties properties = ApplicationConfig.getProducerProperties();
        kafkaProducer = new KafkaProducer<String, String>(properties);
    }

    public void publish(Order order) throws InterruptedException, ExecutionException, TimeoutException {
        String value = new Gson().toJson(order);
        ProducerRecord<String, String> record = new ProducerRecord<>(ApplicationConfig.ORDER_TOPIC, order.getOrderID(), value);

        //Q : synchronous ?

        Future<RecordMetadata> send = kafkaProducer.send(record);
        send.get(ApplicationConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
    }


}
