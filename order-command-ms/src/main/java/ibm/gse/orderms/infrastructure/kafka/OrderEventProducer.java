package ibm.gse.orderms.infrastructure.kafka;

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


    public OrderEventProducer() {
        Properties properties = KafkaInfrastructureConfig.getProducerProperties("ordercmd-event-producer");
        kafkaProducer = new KafkaProducer<String, String>(properties);
    }

    @Override
    public void emit(OrderEventBase event) throws InterruptedException, ExecutionException, TimeoutException {
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
            kafkaProducer.close();
        } catch (Exception e) {
            logger.warn("Failed closing Producer", e);
        }
    }

}
