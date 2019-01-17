package ibm.labs.kc.order.query.kafka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import com.google.gson.Gson;

import ibm.labs.kc.order.query.model.Order;


public class OrderConsumer {
    private static final Logger logger = Logger.getLogger(OrderConsumer.class.getName());
    private static OrderConsumer instance;
    private final KafkaConsumer<String, String> kafkaConsumer;
    
    public synchronized static OrderConsumer instance() {
        if (instance == null) {
            instance = new OrderConsumer();
        }
        return instance;
    }

    public OrderConsumer() {
        Properties properties = ApplicationConfig.getConsumerProperties();
        kafkaConsumer = new KafkaConsumer<String, String>(properties);
        kafkaConsumer.subscribe(Collections.singletonList(ApplicationConfig.ORDER_TOPIC), new ConsumerRebalanceListener() {
            
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                logger.info("Partitions revoked " + partitions);
            }
            
            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                logger.info("Partitions assigned " + partitions);
            }
        });
    }

    public List<Order> poll() {
        List<Order> result = new ArrayList<>();
        Gson gson = new Gson();
        ConsumerRecords<String, String> recs = kafkaConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
        for (ConsumerRecord<String, String> rec : recs) {
            String orderString = rec.value();
            Order o = gson.fromJson(orderString, Order.class);
            result.add(o);
        }
        return result;
    }

    public void close() {
        kafkaConsumer.close(ApplicationConfig.CONSUMER_CLOSE_TIMEOUT);
    }


}
