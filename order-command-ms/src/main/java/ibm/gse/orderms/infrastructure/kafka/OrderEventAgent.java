package ibm.gse.orderms.infrastructure.kafka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.command.events.AssignContainerEvent;
import ibm.gse.orderms.infrastructure.command.events.AssignOrderEvent;
import ibm.gse.orderms.infrastructure.command.events.CancelOrderEvent;
import ibm.gse.orderms.infrastructure.events.Cancellation;
import ibm.gse.orderms.infrastructure.events.ContainerAssignment;
import ibm.gse.orderms.infrastructure.events.Event;
import ibm.gse.orderms.infrastructure.events.EventListener;
import ibm.gse.orderms.infrastructure.events.OrderEvent;
import ibm.gse.orderms.infrastructure.events.VoyageAssignment;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;


public class OrderEventAgent implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventAgent.class.getName());
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final KafkaConsumer<String, String> reloadConsumer;
    private final ShippingOrderRepository orderRepository; 
    
    private boolean initDone = false;
    private OffsetAndMetadata reloadLimit;
    private boolean reloadCompleted = false;

    public OrderEventAgent() {
        Properties properties = ApplicationConfig.getConsumerProperties("ordercmd-event-consumer");
        kafkaConsumer = new KafkaConsumer<String, String>(properties);

        Properties reloadProperties = ApplicationConfig.getConsumerReloadProperties("ordercmd-order-reload-consumer");
        reloadConsumer = new KafkaConsumer<String, String>(reloadProperties);
        orderRepository = new ShippingOrderRepositoryMock();
    }
    
    

    public List<OrderEvent> pollForReload() {
        if (!initDone) {
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

            // blocking call !
            // TODO - need to handle multiple partitions
            reloadLimit = kafkaConsumer.committed(new TopicPartition(ApplicationConfig.ORDER_TOPIC, 0));
            logger.info("Reload limit " + reloadLimit);
            initDone = true;
        }

        List<OrderEvent> result = new ArrayList<>();
        if (reloadLimit==null) {
            // no prior commits found
            reloadCompleted = true;
        } else {ConsumerRecords<String, String> recs = reloadConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
            for (ConsumerRecord<String, String> rec : recs) {
                if (rec.offset() <= reloadLimit.offset()) {
                    OrderEvent event = OrderEvent.deserialize(rec.value());
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

    public List<OrderEvent> poll() {
        ConsumerRecords<String, String> recs = kafkaConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
        List<OrderEvent> result = new ArrayList<>();
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
            logger.warn("Failed closing Consumer", e);
        }
    }

    public void safeReloadClose() {
        try {
            reloadConsumer.close(ApplicationConfig.CONSUMER_CLOSE_TIMEOUT);
        } catch (Exception e) {
            logger.warn("Failed closing reload Consumer",e);
        }
    }

	@Override
	public void handle(Event event) {
		 try {
	            OrderEvent orderEvent = (OrderEvent) event;
	            logger.info("@@@@ in handle " + new Gson().toJson(orderEvent));
	            switch (orderEvent.getType()) {
	            
	            case OrderEvent.TYPE_ASSIGNED:
	                synchronized (orderRepository) {
	                    VoyageAssignment voyageAssignment = ((AssignOrderEvent) orderEvent).getPayload();
	                    String orderID = voyageAssignment.getOrderID();
	                    Optional<ShippingOrder> oco = orderRepository.getByID(orderID);
	                    if (oco.isPresent()) {
	                        ShippingOrder shippingOrder = oco.get();
	                        shippingOrder.assign(voyageAssignment);
	                        orderRepository.update(shippingOrder);
	                    } else {
	                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
	                    }
	                }
	                break;
	            case OrderEvent.TYPE_CANCELLED:
	                synchronized (orderRepository) {
	                    Cancellation cancellation = ((CancelOrderEvent) orderEvent).getPayload();
	                    String orderID = cancellation.getOrderID();
	                    Optional<ShippingOrder> oco = orderRepository.getByID(orderID);
	                    if (oco.isPresent()) {
	                        ShippingOrder shippingOrder = oco.get();
	                        shippingOrder.cancel(cancellation);
	                        orderRepository.update(shippingOrder);
	                    } else {
	                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
	                    }
	                }
	                break;
	            case OrderEvent.TYPE_CONTAINER_ALLOCATED:
	            	synchronized (orderRepository) {
	            		ContainerAssignment ca = ((AssignContainerEvent) orderEvent).getPayload();
		            	String orderID = ca.getOrderID();
		            	Optional<ShippingOrder>oco = orderRepository.getByID(orderID);
		            	if (oco.isPresent()) {
		                     ShippingOrder shippingOrder = oco.get();
		                     shippingOrder.assignContainer(ca);
		                     orderRepository.update(shippingOrder);
		                } else {
		                    throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
		                }
	            	}
	            	break;
	            default:
	                logger.warn("Unknown event type: " + orderEvent);
	            }
	        } catch (Exception e) {
	            logger.error((new Date()).toString() + " " + e.getMessage(), e);
	        }
		
	}

}
