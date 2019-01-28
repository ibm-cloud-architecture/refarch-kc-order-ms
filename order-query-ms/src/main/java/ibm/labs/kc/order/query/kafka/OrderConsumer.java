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

import ibm.labs.kc.order.query.model.events.OrderEvent;

public class OrderConsumer {
    private static final Logger logger = Logger.getLogger(OrderConsumer.class.getName());
    private final KafkaConsumer<String, String> kafkaConsumer;
    private boolean subscribeCalled = false;

    public OrderConsumer() {
        Properties properties = ApplicationConfig.getConsumerProperties();
        kafkaConsumer = new KafkaConsumer<>(properties);
    }

    public List<OrderEvent> poll() {
        if (!subscribeCalled) {
            kafkaConsumer.subscribe(Collections.singletonList(ApplicationConfig.ORDER_TOPIC),
                    new ConsumerRebalanceListener() {

                        @Override
                        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                            logger.info("Partitions revoked " + partitions);
                        }

                        @Override
                        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                            logger.info("Partitions assigned " + partitions);
                        }
                    });
            subscribeCalled = true;
        }

        List<OrderEvent> result = new ArrayList<>();
        ConsumerRecords<String, String> recs = kafkaConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
        for (ConsumerRecord<String, String> rec : recs) {
            OrderEvent event = OrderEvent.deserialize(rec.value());
            result.add(event);
        }
        return result;
    }

    public void safeClose() {
        try {
            kafkaConsumer.close(ApplicationConfig.CONSUMER_CLOSE_TIMEOUT);
        } catch (Exception e) {
            logger.warning("Failed closing Consumer");
        }
    }

}
