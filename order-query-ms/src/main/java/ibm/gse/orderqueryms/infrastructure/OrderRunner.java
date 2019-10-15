package ibm.gse.orderqueryms.infrastructure;

import java.util.List;

import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderqueryms.infrastructure.events.order.OrderEvent;
import ibm.gse.orderqueryms.infrastructure.kafka.OrderAgent;

public class OrderRunner implements Runnable {
	
	static final Logger logger = LoggerFactory.getLogger(OrderRunner.class);
	
	private volatile boolean running = true;

	public OrderRunner() {

	}
	
	public void stop() {
		this.running = false;
	}

	@Override
	public void run() {
		
		//NEW
		logger.info("Order event consumer loop thread started");
		OrderAgent orderEventAgent = new OrderAgent();
        boolean ok = true;
        try {
            while (running && ok) {
                try {
                    List<OrderEvent> events = orderEventAgent.poll();
                    for (OrderEvent event : events) {
                    	orderEventAgent.handle(event);
                    }
                } catch (KafkaException ke) {
                    // Treat a Kafka exception as unrecoverable
                    // stop this task and queue a new one
                    ok = false;
                }
            }
        } finally {
        	orderEventAgent.safeClose();  
        }
        
        
		/*
		///OLD
		logger.info("ConsumerLoop thread started");
        EventListener queryServiceListener = new QueryService();
        EventListener orderActionServicelistener = new OrderActionService();
        
        EventEmitter emitter = new ErrorProducer();
        boolean ok = true;
        try {
            while (running && ok) {
                try {
                    List<OrderEvent> ordEvents = orderConsumer.poll();
                    for (OrderEvent event : ordEvents) {
                        try {
                        	queryServiceListener.handle(event, "order");
                        	orderActionServicelistener.handle(event, "order");
                        } catch (Exception e) {
                            e.printStackTrace();
                            ErrorEvent errorEvent = new ErrorEvent(System.currentTimeMillis(),
                                    ErrorEvent.TYPE_ERROR, "1", event, e.getMessage());
                            try {
                                emitter.emit(errorEvent);
                            } catch (Exception e1) {
                                logger.error("Failed emitting Error event " + errorEvent, e1);
                            }
                        }
                    }
                    
                } catch (Exception ke) {
                    ke.printStackTrace();
                    // Treat a Kafka exception as unrecoverable
                    // stop this task and queue a new one
                    ok = false;
                    orderExecutor.execute(newOrderRunnable());
                    // TODO: after a number of retrying, mark app as unhealthy for external monitoring restart
                }
            }
        } finally {
        	orderConsumer.safeClose();
            emitter.safeClose();
        }
        */

	}

}
