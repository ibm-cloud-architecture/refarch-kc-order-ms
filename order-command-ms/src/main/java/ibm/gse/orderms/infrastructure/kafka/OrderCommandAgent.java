package ibm.gse.orderms.infrastructure.kafka;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.AppRegistry;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.events.EventListener;
import ibm.gse.orderms.infrastructure.events.OrderEvent;
import ibm.gse.orderms.infrastructure.events.OrderEventBase;
import ibm.gse.orderms.infrastructure.repository.OrderCreationException;
import ibm.gse.orderms.infrastructure.repository.OrderUpdateException;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;

/**
 * Order command agent listens to commands on the shipping order, that
 * are published to the order commands topic.
 *
 * It uses the repository to save the newly create order or update and existing one.
 * 
 * Once the persistence is done, it emits events for other service to consume.
 */
@ApplicationScoped
public class OrderCommandAgent implements EventListener {
	 
	  private static final Logger logger = LoggerFactory.getLogger(OrderCommandAgent.class.getName());
	  
	  private final KafkaConsumer<String, String> orderCommandsConsumer;
	  private ShippingOrderRepository orderRepository; 
	  private EventEmitter orderEventProducer;
	  private EventEmitter errorEventProducer;
	  
	  private Duration pollTimeOut;
	  private Duration closeTimeOut;
	  private String schemaVersion;
	  private boolean running = true;
	  
	  
	  public OrderCommandAgent() {
	      Properties properties = KafkaInfrastructureConfig.getConsumerProperties("ordercmd-command-consumer-grp",
	    		  "ordercmd-command-consumer",false,"earliest");
	      this.orderCommandsConsumer = new KafkaConsumer<String, String>(properties);
	      this.orderRepository = AppRegistry.getInstance().shippingOrderRepository();
	      this.orderCommandsConsumer.subscribe(Collections.singletonList(KafkaInfrastructureConfig.getOrderCommandTopic()));
	      this.orderEventProducer = AppRegistry.getInstance().orderEventProducer();
	      this.errorEventProducer = AppRegistry.getInstance().errorEventProducer();
	      this.pollTimeOut = KafkaInfrastructureConfig.CONSUMER_POLL_TIMEOUT;
	      this.closeTimeOut = KafkaInfrastructureConfig.CONSUMER_CLOSE_TIMEOUT;
	      this.schemaVersion = KafkaInfrastructureConfig.SCHEMA_VERSION;
	  }
	  
	  /**
	   * Constructor used for unit testing
	   * @param repo
	   * @param kafka
	   * @param oee
	   */
	  public OrderCommandAgent(ShippingOrderRepository repo, 
			  				KafkaConsumer<String, String>  kafka, 
			  				EventEmitter oee,
			  				EventEmitter eep) {
		  this.orderCommandsConsumer = kafka;
		  this.orderRepository = repo;
		  this.orderEventProducer = oee;
		  this.errorEventProducer = eep;
		  this.pollTimeOut = Duration.of(10, ChronoUnit.SECONDS);
	     this.closeTimeOut = Duration.of(10, ChronoUnit.SECONDS);
	     this.schemaVersion = "1";
	  }
	  
	  /** 
	   * Get n records from the order command topic
	   * In case of timeout poll returns empty list.
	   * @return FIFO list command events
	   */
	  public List<OrderCommandEvent> poll() {
		 // The kafka consumer poll api ensures liveness. The consumer sends periodic heartbeats to the server
        ConsumerRecords<String, String> recs = this.orderCommandsConsumer.poll(this.pollTimeOut);
        List<OrderCommandEvent> result = new ArrayList<>();
        for (ConsumerRecord<String, String> rec : recs) {
        	logger.info("Command event received: " + rec.value());
            OrderCommandEvent event = OrderCommandEvent.deserialize(rec.value());
            result.add(event);
        }
        return result;
	  }

	  public void safeClose() {
        try {
            orderCommandsConsumer.close(this.closeTimeOut);
        } catch (Exception e) {
            logger.warn("Failed closing Consumer", e);
        }
	    }

	@Override
	public void handle(OrderEventBase event) {
		
		OrderCommandEvent commandEvent = (OrderCommandEvent)event;
		logger.info("handle command event : " + commandEvent.getType());
		
		switch (commandEvent.getType()) {
        case OrderCommandEvent.TYPE_CREATE_ORDER:
        	processOrderCreation(commandEvent);
            break;
        case OrderCommandEvent.TYPE_UPDATE_ORDER:
        	processOrderUpdate(commandEvent);
            break;
		}
	}
	
	/**
	 * Handle create order command: persist order into repository and emit event 'order created'
	 * for others to consume. The order is in pending mode until others services responded
	 * with a Voyage and a Reefer assignment events. When the 'order created` event is generated, the
	 * read from command topic can be committed. It commits the offset only 
	 * when both save to the repository and send order created events succeed. 
	 * 
	 * If it is not able to persist, it does not emit event on orders topic, 
	 * but emits on errors topic. The error topics is managed downstream by other component like a CLI 
	 * or an automatic recovery process. The data are not lost as they are still in order command topic.  
	 * 
	 * If publishing to any topic, even after retries, fails then the approach is 
	 * to die and let the scheduler recreate the app, and connection to kafka may be 
	 * recovered. 
	 * 
	 * The order is still in pending and the offset is not committed. So it is possible
	 * to restart the app and it will reload from the last committed offset. So the data
	 * may be reprocessed, therefore the repository should do nothing in this duplicate info
	 * if it found the order already created.
	 *  
	 * @param commandEvent
	 */
	private void processOrderCreation(OrderCommandEvent commandEvent ) {
		ShippingOrder shippingOrder = (ShippingOrder) commandEvent.getPayload();
		shippingOrder.setStatus(ShippingOrder.PENDING_STATUS);
		try {
    		orderRepository.addOrUpdateNewShippingOrder(shippingOrder);	
		} catch (OrderCreationException e) {
			// need other components to fix this save operation: CLI / human or automatic process
			generateErrorEvent(shippingOrder);
    		return ; 
    	}
        OrderEvent orderCreatedEvent = new OrderEvent(new Date().getTime(),
                		OrderEvent.TYPE_ORDER_CREATED,
                		schemaVersion,
                		shippingOrder.toShippingOrderPayload());
        try {
        	orderEventProducer.emit(orderCreatedEvent);
        	orderCommandsConsumer.commitSync();
		} catch (Exception e) {
			// the order is in the repository but the app could not send to event backbone
			// consider communication with backbone as major issue
			e.printStackTrace();
			running = false; // liveness may kill this app and restart it
			return ;
		}   
	}
	
	/**
	 * For the order update 
	 * @param commandEvent
	 */
	private void processOrderUpdate(OrderCommandEvent commandEvent) {
	    ShippingOrder shippingOrder = (ShippingOrder) commandEvent.getPayload();
        String orderID = shippingOrder.getOrderID();
        try {
        	 Optional<ShippingOrder> oco = orderRepository.getOrderByOrderID(orderID);
        	 if (oco.isPresent()) {
        		 orderRepository.updateShippingOrder(shippingOrder);
        		 OrderEvent orderUpdateEvent = new OrderEvent(new Date().getTime(),
         			  	OrderEvent.TYPE_ORDER_UPDATED,
         			    schemaVersion,
         			  	shippingOrder.toShippingOrderPayload());
        		 try {
	        		 orderEventProducer.emit(orderUpdateEvent);
	        		 this.orderCommandsConsumer.commitSync();
        		 } catch (Exception e) {
        			e.printStackTrace();
        			running = false; // liveness may kill this app and restart it
        		    return ;
        		 }
        	 } else {
        		logger.error("Cannot update order - Unknown order Id " + orderID);
        		generateErrorEvent(shippingOrder);
        	 }
        } catch (OrderUpdateException oue) {
        	generateErrorEvent(shippingOrder);
        }
	}

	public boolean isRunning() {
		return this.running;
	}
	
	private void generateErrorEvent(ShippingOrder shippingOrder) {
		ErrorEvent errorEvent = new ErrorEvent(new Date().getTime(), 
				schemaVersion, 
				shippingOrder.toShippingOrderPayload(),
				"Repository access issue");
		try {
			errorEventProducer.emit(errorEvent);
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error("Error event production error for order: " + shippingOrder.getOrderID());
		}
	}
}
