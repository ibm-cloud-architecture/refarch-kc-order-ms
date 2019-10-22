package ibm.gse.orderms.infrastructure.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
	  
	  public OrderCommandAgent() {
	      Properties properties = KafkaInfrastructureConfig.getConsumerProperties("ordercmd-command-consumer-grp",
	    		  "ordercmd-command-consumer",false,"earliest");
	      this.orderCommandsConsumer = new KafkaConsumer<String, String>(properties);
	      this.orderRepository = AppRegistry.getInstance().shippingOrderRepository();
	      this.orderCommandsConsumer.subscribe(Collections.singletonList(KafkaInfrastructureConfig.ORDER_COMMAND_TOPIC));
	      this.orderEventProducer = AppRegistry.getInstance().orderEventProducer();
	  }
	  
	  public OrderCommandAgent(ShippingOrderRepository repo, KafkaConsumer<String, String>  kafka, EventEmitter oee) {
		  this.orderCommandsConsumer = kafka;
		  this.orderRepository = repo;
		  this.orderCommandsConsumer.subscribe(Collections.singletonList(KafkaInfrastructureConfig.ORDER_COMMAND_TOPIC));
		  this.orderEventProducer = oee;
	  }
	  
	  /** 
	   * Get n records from the order command topic
	   * 
	   * @return FIFO list command events
	   */
	  public List<OrderCommandEvent> poll() {
		 // The kafka consumer poll api ensures liveness. The consumer sends periodic heartbeats to the server
        ConsumerRecords<String, String> recs = this.orderCommandsConsumer.poll(KafkaInfrastructureConfig.CONSUMER_POLL_TIMEOUT);
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
            orderCommandsConsumer.close(KafkaInfrastructureConfig.CONSUMER_CLOSE_TIMEOUT);
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
	
	private void processOrderCreation(OrderCommandEvent commandEvent ) {
		ShippingOrder shippingOrder = (ShippingOrder) commandEvent.getPayload();	
		try {
    		synchronized (orderRepository) {
                orderRepository.addNewShippingOrder(shippingOrder);
            }	
    	} catch (Exception e) {
    		// TODO with remote datasource write to a error log
    		e.printStackTrace();
    		return ; 
    	}
		
        OrderEvent orderCreatedEvent = new OrderEvent(new Date().getTime(),
        		OrderEvent.TYPE_ORDER_CREATED,
        		KafkaInfrastructureConfig.SCHEMA_VERSION,
        		shippingOrder.toShippingOrderPayload());
        try {
        	orderEventProducer.emit(orderCreatedEvent);
		} catch (Exception e) {
			// TODO 
			e.printStackTrace();
			return ;
		}
        this.orderCommandsConsumer.commitSync();
	}
	
	
	private void processOrderUpdate(OrderCommandEvent commandEvent) {
	    ShippingOrder shippingOrder = (ShippingOrder) commandEvent.getPayload();
        String orderID = shippingOrder.getOrderID();
   
        Optional<ShippingOrder> oco = orderRepository.getOrderByOrderID(orderID);
        if (oco.isPresent()) {
              try {
            	  synchronized (orderRepository) {
            		  orderRepository.updateShippingOrder(shippingOrder);
            	  }
              } catch (Exception e ) {
            	  e.printStackTrace();
            	  return ;
              }
                
              try {
            	  OrderEvent orderUpdateEvent = new OrderEvent(new Date().getTime(),
            			  	OrderEvent.TYPE_ORDER_UPDATED,
            			    KafkaInfrastructureConfig.SCHEMA_VERSION,
            			  	shippingOrder.toShippingOrderPayload());
            	  orderEventProducer.emit(orderUpdateEvent);
			} catch (Exception e) {
				e.printStackTrace();
				return ;
			}
            this.orderCommandsConsumer.commitSync();
        } else {
                throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
        }
            
	}
}
