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
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import ibm.labs.kc.order.query.model.events.OrderEvent;

public class OrderConsumer {
    private static final Logger logger = Logger.getLogger(OrderConsumer.class.getName());
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final KafkaConsumer<String, String> reloadConsumer;

    private OffsetAndMetadata reloadLimit;
    private boolean reloadCompleted = false;

    public OrderConsumer() {
        Properties properties = ApplicationConfig.getConsumerProperties();
        kafkaConsumer = new KafkaConsumer<>(properties);

        Properties reloadProperties = ApplicationConfig.getConsumerReloadProperties();
        reloadConsumer = new KafkaConsumer<String, String>(reloadProperties);
    }

    public List<OrderEvent> pollForReload() {
        if (reloadLimit == null) {
            reloadConsumer.subscribe(
                    Collections.singletonList(ApplicationConfig.ORDER_TOPIC),
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

            kafkaConsumer.subscribe(
                    Collections.singletonList(ApplicationConfig.ORDER_TOPIC),
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

            // blocking call !
            // TODO - need to handle multiple partitions
            reloadLimit = kafkaConsumer.committed(new TopicPartition(ApplicationConfig.ORDER_TOPIC, 0));
        }

        ConsumerRecords<String, String> recs = reloadConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
        List<OrderEvent> result = new ArrayList<>();
        for (ConsumerRecord<String, String> rec : recs) {
            if (rec.offset() <= reloadLimit.offset()) {
                OrderEvent event = OrderEvent.deserialize(rec.value());
                result.add(event);
            } else {
                logger.warning("Reload Completed");
                reloadCompleted = true;
                break;
            }
        }
        return result;
    }

    public boolean reloadCompleted() {
        return reloadCompleted;
    }

    public List<OrderEvent> poll() {
        ConsumerRecords<String, String> recs = kafkaConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
        List<OrderEvent> result = new ArrayList<>();
        for (ConsumerRecord<String, String> rec : recs) {
            OrderEvent event = OrderEvent.deserialize(rec.value());
            result.add(event);
        }
        return result;
    }

    public void safeReloadClose() {
        try {
            reloadConsumer.close(ApplicationConfig.CONSUMER_CLOSE_TIMEOUT);
        } catch (Exception e) {
            logger.warning("Failed closing Consumer");
        }
    }


    public void safeClose() {
        try {
            kafkaConsumer.close(ApplicationConfig.CONSUMER_CLOSE_TIMEOUT);
        } catch (Exception e) {
            logger.warning("Failed closing Consumer");
        }
    }


}
