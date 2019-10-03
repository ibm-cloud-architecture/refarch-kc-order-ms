package ibm.gse.orderms.infrastructure.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.command.events.CreateOrderCommandEvent;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.command.events.UpdateOrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.Event;
import ibm.gse.orderms.infrastructure.events.EventListener;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;

public class OrderCommandAgent implements EventListener {
	  private static final Logger logger = LoggerFactory.getLogger(OrderCommandAgent.class.getName());
	  
	  private final KafkaConsumer<String, String> kafkaConsumer;
	  private final ShippingOrderRepository orderRepository; 
	  
	  public OrderCommandAgent() {
	      Properties properties = ApplicationConfig.getConsumerProperties("ordercmd-command-consumer");
	      this.kafkaConsumer = new KafkaConsumer<String, String>(properties);
	      this.orderRepository = new ShippingOrderRepositoryMock();
	  }
	  
	  public OrderCommandAgent(ShippingOrderRepository repo, KafkaConsumer<String, String>  kafka) {
		  this.kafkaConsumer = kafka;
		  this.orderRepository = repo;
	  }
	  
	  public List<OrderCommandEvent> poll() {
        ConsumerRecords<String, String> recs = this.kafkaConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
        List<OrderCommandEvent> result = new ArrayList<>();
        for (ConsumerRecord<String, String> rec : recs) {
            OrderCommandEvent event = OrderCommandEvent.deserialize(rec.value());
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

	@Override
	public void handle(Event event) {
		
		OrderCommandEvent commandEvent = (OrderCommandEvent)event;
		
		switch (commandEvent.getType()) {
        case OrderCommandEvent.TYPE_CREATE_ORDER:
            synchronized (orderRepository) {
                ShippingOrder shippingOrder = ((CreateOrderCommandEvent) commandEvent).getPayload();
                orderRepository.addNewShippingOrder(shippingOrder);
            }
            break;
        case OrderCommandEvent.TYPE_UPDATE_ORDER:
            synchronized (orderRepository) {
                ShippingOrder shippingOrder = ((UpdateOrderCommandEvent) commandEvent).getPayload();
                String orderID = shippingOrder.getOrderID();
                Optional<ShippingOrder> oco = orderRepository.getByID(orderID);
                if (oco.isPresent()) {
                    orderRepository.update(shippingOrder);
                } else {
                    throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                }
            }
            break;
		}
	}
}
