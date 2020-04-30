package ibm.gse.orderms.infrastructure.kafka;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.AppRegistry;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.events.EventListener;
import ibm.gse.orderms.infrastructure.events.EventBase;
import ibm.gse.orderms.infrastructure.events.order.OrderCancelledEvent;
import ibm.gse.orderms.infrastructure.events.order.OrderEvent;
import ibm.gse.orderms.infrastructure.events.order.OrderRejectEvent;
import ibm.gse.orderms.infrastructure.events.order.OrderSpoiltEvent;
import ibm.gse.orderms.infrastructure.events.order.OrderSpoiltPayload;
import ibm.gse.orderms.infrastructure.events.container.ContainerAllocatedEvent;
import ibm.gse.orderms.infrastructure.events.container.ContainerNotFoundEvent;
import ibm.gse.orderms.infrastructure.events.container.ContainerNotFoundPayload;
import ibm.gse.orderms.infrastructure.events.container.ContainerAllocatedPayload;
import ibm.gse.orderms.infrastructure.events.voyage.VoyageAssignedEvent;
import ibm.gse.orderms.infrastructure.events.voyage.VoyageNotFoundEvent;
import ibm.gse.orderms.infrastructure.events.voyage.VoyageNotFoundPayload;
import ibm.gse.orderms.infrastructure.events.voyage.VoyageAssignmentPayload;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;


/**
 * Kafka consumer to get order events from other services or from this command
 * service
 * 
 * 
 * @author jerome boyer
 *
 */
public class OrderEventAgent implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventAgent.class.getName());
	private final KafkaConsumer<String, String> kafkaConsumer;
	private EventEmitter orderEventProducer; // Need to produce OrderRejected Events
    private final ShippingOrderRepository orderRepository; 
    private static final Gson gson = new Gson();
    private Duration pollTimeOut;
	private Duration closeTimeOut;
	private String schemaVersion;
    private boolean running = true;

    public OrderEventAgent() {
		Properties properties = KafkaInfrastructureConfig.getConsumerProperties("ordercmd-event-consumer-grp", "OrderEventAgent", true,"earliest");
		// Using a value of read_committed ensures that we don't read any transactional
		// messages before the transaction from OrderCommandAgent to create, update or reject an order completes.
		properties.put("isolation.level", "read_committed");
        kafkaConsumer = new KafkaConsumer<String, String>(properties);
        this.kafkaConsumer.subscribe(Collections.singletonList(KafkaInfrastructureConfig.getOrderTopic()));
        orderRepository = AppRegistry.getInstance().shippingOrderRepository();	
        this.pollTimeOut = KafkaInfrastructureConfig.CONSUMER_POLL_TIMEOUT;
		this.closeTimeOut = KafkaInfrastructureConfig.CONSUMER_CLOSE_TIMEOUT;
		this.orderEventProducer = AppRegistry.getInstance().orderEventProducer();
		this.schemaVersion = KafkaInfrastructureConfig.SCHEMA_VERSION;
    }
    
    /**
     * Constructor used for unit testing
     * @param kafkaConsumer
     * @param orderRepository
     */
    public OrderEventAgent(KafkaConsumer<String, String> kafkaConsumer, ShippingOrderRepository orderRepository) {
    	this.kafkaConsumer = kafkaConsumer;
    	this.orderRepository = orderRepository;
    	this.pollTimeOut = Duration.of(10, ChronoUnit.SECONDS);
		this.closeTimeOut = Duration.of(10, ChronoUnit.SECONDS);
		this.schemaVersion="1";
		this.orderEventProducer = null; // TBD: create orderEventProducer attribute. Will need to adjust unit test
    }
    
    public List<EventBase> poll() {
        ConsumerRecords<String, String> recs = kafkaConsumer.poll(this.pollTimeOut);
        List<EventBase> result = new ArrayList<>();
        for (ConsumerRecord<String, String> rec : recs) {
        	EventBase event = deserialize(rec.value());
            result.add(event);
        }
        return result;
    }
    
    public EventBase deserialize(String eventAsString) {
    	EventBase orderEvent = gson.fromJson(eventAsString, EventBase.class);
        switch (orderEvent.getType()) {
            case EventBase.TYPE_ORDER_CREATED:
			case EventBase.TYPE_ORDER_UPDATED:
				return gson.fromJson(eventAsString, OrderEvent.class);
			case EventBase.TYPE_ORDER_REJECTED:
				return gson.fromJson(eventAsString, OrderRejectEvent.class);
            case EventBase.TYPE_VOYAGE_ASSIGNED:
				return gson.fromJson(eventAsString, VoyageAssignedEvent.class);
			case EventBase.TYPE_CONTAINER_NOT_FOUND:
				return gson.fromJson(eventAsString, ContainerNotFoundEvent.class);
			case EventBase.TYPE_VOYAGE_NOT_FOUND:
                return gson.fromJson(eventAsString, VoyageNotFoundEvent.class);
            case EventBase.TYPE_ORDER_CANCELLED:
                return gson.fromJson(eventAsString, OrderCancelledEvent.class);
            case EventBase.TYPE_CONTAINER_ALLOCATED:
                return gson.fromJson(eventAsString, ContainerAllocatedEvent.class);
            case EventBase.TYPE_ORDER_SPOILT:
           	    return gson.fromJson(eventAsString, OrderSpoiltEvent.class);
            default:
                logger.warn("Not supported event: " + eventAsString);
                return null;
        }
    }

    public void safeClose() {
        try {
            kafkaConsumer.close(this.closeTimeOut);
        } catch (Exception e) {
            logger.warn("Failed closing Consumer", e);
        }
    }

	@Override
	public void handle(EventBase orderEvent) {
		 try {
	  
	            logger.info("@@@@ in handle " + new Gson().toJson(orderEvent));
	            if (orderEvent == null) return;
	            switch (orderEvent.getType()) {
	            case EventBase.TYPE_VOYAGE_ASSIGNED:
	                synchronized (orderRepository) {
	                	VoyageAssignmentPayload voyageAssignment = (VoyageAssignmentPayload)((VoyageAssignedEvent) orderEvent).getPayload();
	                    String orderID = voyageAssignment.getOrderID();
	                    Optional<ShippingOrder> oco = orderRepository.getOrderByOrderID(orderID);
	                    if (oco.isPresent()) {
	                        ShippingOrder shippingOrder = oco.get();
	                        shippingOrder.assign(voyageAssignment);
	                        orderRepository.updateShippingOrder(shippingOrder);
	                    } else {
	                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
	                    }
	                }
	                break;
	            case EventBase.TYPE_CONTAINER_ALLOCATED:
	            	synchronized (orderRepository) {
	            		ContainerAllocatedPayload ca = ((ContainerAllocatedEvent) orderEvent).getPayload();
		            	String orderID = ca.getOrderID();
		            	Optional<ShippingOrder> oco = orderRepository.getOrderByOrderID(orderID);
		            	if (oco.isPresent()) {
		                     ShippingOrder shippingOrder = oco.get();
		                     shippingOrder.assignContainer(ca);
		                     orderRepository.updateShippingOrder(shippingOrder);
		                } else {
		                    throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
		                }
	            	}
                    break;
                case EventBase.TYPE_ORDER_SPOILT:
	            	synchronized (orderRepository) {
	            		OrderSpoiltPayload os = ((OrderSpoiltEvent) orderEvent).getPayload();
		            	String orderID = os.getOrderID();
		            	Optional<ShippingOrder> oco = orderRepository.getOrderByOrderID(orderID);
		            	if (oco.isPresent()) {
		                     ShippingOrder shippingOrder = oco.get();
		                     shippingOrder.spoilOrder();
		                     orderRepository.updateShippingOrder(shippingOrder);
		                } else {
		                    throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
		                }
	            	}
					break;
				case EventBase.TYPE_CONTAINER_NOT_FOUND:
	            	synchronized (orderRepository) {
	            		ContainerNotFoundPayload payload = ((ContainerNotFoundEvent) orderEvent).getPayload();
		            	String orderID = payload.getOrderID();
		            	Optional<ShippingOrder> oco = orderRepository.getOrderByOrderID(orderID);
		            	if (oco.isPresent()) {
		                     ShippingOrder shippingOrder = oco.get();
		                     shippingOrder.rejectOrder();
							 orderRepository.updateShippingOrder(shippingOrder);
							 sendOrderRejectEvent(shippingOrder, payload.getReason());
		                } else {
		                    throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
		                }
	            	}
	            	break;
				case EventBase.TYPE_VOYAGE_NOT_FOUND:
	            	synchronized (orderRepository) {
	            		VoyageNotFoundPayload payload = ((VoyageNotFoundEvent) orderEvent).getPayload();
		            	String orderID = payload.getOrderID();
		            	Optional<ShippingOrder> oco = orderRepository.getOrderByOrderID(orderID);
		            	if (oco.isPresent()) {
		                     ShippingOrder shippingOrder = oco.get();
		                     shippingOrder.rejectOrder();
							 orderRepository.updateShippingOrder(shippingOrder);
							 sendOrderRejectEvent(shippingOrder, payload.getReason());
		                } else {
		                    throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
		                }
	            	}
	            	break;
	            case EventBase.TYPE_ORDER_CREATED:
				case EventBase.TYPE_ORDER_UPDATED:
				case EventBase.TYPE_ORDER_CANCELLED:
				case EventBase.TYPE_ORDER_REJECTED:
	            	break;
	            default:
	                logger.warn("Not yet implemented event type: " + orderEvent.getType());
	            }
	        } catch (Exception e) {
	            logger.error((new Date()).toString() + " " + e.getMessage(), e);
	        }
		
	}

	public boolean isRunning() {
		return this.running;
	}
	
	public void sendOrderRejectEvent (ShippingOrder shippingOrder, String reason){
		OrderRejectEvent orderRejectedEvent = new OrderRejectEvent(new Date().getTime(), schemaVersion, shippingOrder.toOrderCancelAndRejectPayload(reason));
		try {
			orderEventProducer.emit(orderRejectedEvent);
		} catch (Exception e) {
			// the order is in the repository but the app could not send to event backbone
			// consider communication with backbone as major issue
			e.printStackTrace();
			running = false; // liveness may kill this app and restart it
		}
	}
}
