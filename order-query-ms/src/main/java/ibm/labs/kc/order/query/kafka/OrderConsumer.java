package ibm.labs.kc.order.query.kafka;

import java.util.ArrayList;
import java.util.Arrays;
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

import ibm.labs.kc.order.query.model.Events;
import ibm.labs.kc.order.query.model.events.ContainerEvent;
import ibm.labs.kc.order.query.model.events.OrderEvent;

public class OrderConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class.getName());
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final KafkaConsumer<String, String> reloadConsumer;

    private boolean initDone = false;
    private OffsetAndMetadata reloadLimitOrder;
    private OffsetAndMetadata reloadLimitContainer;
    private boolean reloadCompleted = false;

    public OrderConsumer() {
        Properties properties = ApplicationConfig.getConsumerProperties();
        kafkaConsumer = new KafkaConsumer<>(properties);

        Properties reloadProperties = ApplicationConfig.getConsumerReloadProperties();
        reloadConsumer = new KafkaConsumer<String, String>(reloadProperties);
    }

    public Events pollForReload() {
        if (!initDone) {
            reloadConsumer.subscribe(
            		Arrays.asList(ApplicationConfig.ORDER_TOPIC, ApplicationConfig.CONTAINER_TOPIC),
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
            		Arrays.asList(ApplicationConfig.ORDER_TOPIC, ApplicationConfig.CONTAINER_TOPIC),
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
            reloadLimitOrder = kafkaConsumer.committed(new TopicPartition(ApplicationConfig.ORDER_TOPIC, 0));
            logger.info("Reload limit Order" + reloadLimitOrder);
            
            reloadLimitContainer = kafkaConsumer.committed(new TopicPartition(ApplicationConfig.CONTAINER_TOPIC, 0));
            logger.info("Reload limit Container" + reloadLimitContainer);
            
            initDone = true;
        }

        List<OrderEvent> orderResult = new ArrayList<>();
        List<ContainerEvent> containerResult = new ArrayList<>();

        if (reloadLimitOrder==null && reloadLimitContainer==null) {
            // no prior commits found
        	reloadCompleted = true;
        } else {
            ConsumerRecords<String, String> recs = reloadConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
            for (ConsumerRecord<String, String> rec : recs) {
                if (rec.offset() <= reloadLimitOrder.offset() || rec.offset() <= reloadLimitContainer.offset()) {
                	if(rec.offset() <= reloadLimitOrder.offset()){
                		OrderEvent ordEvent = OrderEvent.deserialize(rec.value());
                        orderResult.add(ordEvent);
                	}
                    if(rec.offset() <= reloadLimitContainer.offset()){
                    	ContainerEvent conEvent = ContainerEvent.deserialize(rec.value());
                    	containerResult.add(conEvent);
                    }
                } else {
                    logger.info("Reload Completed");
                    reloadCompleted = true;
                    break;
                }
            }
        }
        Events events = new Events(orderResult, containerResult);
        return events;
    }

    public boolean reloadCompleted() {
        return reloadCompleted;
    }

    public Events poll() {
        ConsumerRecords<String, String> recs = kafkaConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
        List<OrderEvent> orderResult = new ArrayList<>();
        List<ContainerEvent> containerResult = new ArrayList<>();
        for (ConsumerRecord<String, String> rec : recs) {
            OrderEvent ordEvent = OrderEvent.deserialize(rec.value());
            orderResult.add(ordEvent);
            ContainerEvent conEvent = ContainerEvent.deserialize(rec.value());
            containerResult.add(conEvent);
        }
        Events events = new Events(orderResult, containerResult);
        return events;
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
