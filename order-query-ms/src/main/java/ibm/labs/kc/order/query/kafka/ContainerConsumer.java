package ibm.labs.kc.order.query.kafka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.labs.kc.order.query.model.events.ContainerEvent;

public class ContainerConsumer {
	
	private static final Logger logger = LoggerFactory.getLogger(ContainerConsumer.class.getName());
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final KafkaConsumer<String, String> reloadConsumer;

    private boolean initDone = false;
    private OffsetAndMetadata reloadLimit;
    private boolean reloadCompleted = false;

    public ContainerConsumer() {
        Properties properties = ApplicationConfig.getContainerConsumerProperties();
        kafkaConsumer = new KafkaConsumer<>(properties);

        Properties reloadProperties = ApplicationConfig.getContainerConsumerReloadProperties();
        reloadConsumer = new KafkaConsumer<String, String>(reloadProperties);
    }

    public List<ContainerEvent> pollForReload() {
        if (!initDone) {
            reloadConsumer.subscribe(
                    Collections.singletonList(ApplicationConfig.CONTAINER_TOPIC),
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
                    Collections.singletonList(ApplicationConfig.CONTAINER_TOPIC),
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
            reloadLimit = kafkaConsumer.committed(new TopicPartition(ApplicationConfig.CONTAINER_TOPIC, 0));
            logger.info("Reload limit " + reloadLimit);
            initDone = true;
        }

        List<ContainerEvent> result = new ArrayList<>();

        if (reloadLimit==null) {
            // no prior commits found
            reloadCompleted = true;
        } else {
            ConsumerRecords<String, String> recs = reloadConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
            for (ConsumerRecord<String, String> rec : recs) {
                if (rec.offset() <= reloadLimit.offset()) {
                    ContainerEvent event = ContainerEvent.deserialize(rec.value());
                    result.add(event);
                } else {
                    logger.info("Reload Completed");
                    reloadCompleted = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean reloadCompleted() {
        return reloadCompleted;
    }

    public List<ContainerEvent> poll() {
        ConsumerRecords<String, String> recs = kafkaConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
        List<ContainerEvent> result = new ArrayList<>();
        for (ConsumerRecord<String, String> rec : recs) {
            ContainerEvent event = ContainerEvent.deserialize(rec.value());
            result.add(event);
        }
        return result;
    }

    public void safeReloadClose() {
        try {
            reloadConsumer.close(ApplicationConfig.CONSUMER_CLOSE_TIMEOUT);
        } catch (Exception e) {
            logger.warn("Failed closing reload Consumer",e);
        }
    }


    public void safeClose() {
        try {
            kafkaConsumer.close(ApplicationConfig.CONSUMER_CLOSE_TIMEOUT);
        } catch (Exception e) {
            logger.warn("Failed closing Consumer",e);
        }
    }



}
