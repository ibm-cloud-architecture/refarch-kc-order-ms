package ibm.gse.orderms.infrastructure;

import java.util.List;

import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;

/**
 * Listen to order command  event to support the command pattern, using the queueing capability
 * of the remote messaging system
 * 
 * @author jerome boyer
 *
 */
public class OrderCommandRunner implements Runnable {
	static final Logger logger = LoggerFactory.getLogger(OrderCommandRunner.class);
	
	private volatile boolean running = true;
	
	public OrderCommandRunner() {
	}
	
	public void stop() {
		this.running = false;
	}
	
	
	@Override
	public void run() {
		logger.info("Order command consumer loop thread started");
		OrderCommandAgent orderCommandAgent = new OrderCommandAgent();
        boolean ok = true;
        try {
            while (running && ok) {
                try {
                    List<OrderCommandEvent> events = orderCommandAgent.poll();
                    // in case of timeout the list is empty.
                    for (OrderCommandEvent event : events) {
                       	orderCommandAgent.handle(event);
                    }
                } catch (KafkaException ke) {
                    // Treat a Kafka exception as unrecoverable
                    // stop this task and queue a new one
                    ok = false;
                }
            }
        } finally {
        	orderCommandAgent.safeClose();  
        }
	}

}


/**
 * 
 *  try {
                        	orderCommandAgent.handle(event);
                        } catch (Exception e) {
                            ErrorEvent errorEvent = new ErrorEvent(System.currentTimeMillis(),
                                    ErrorEvent.TYPE_ERROR, "1", event, e.getMessage());
                            try {
                                emitter.emit(errorEvent);
                            } catch (Exception e1) {
                                logger.error("Failed emitting Error event " + errorEvent, e1);
                            }
                        }
                    }
                } catch (KafkaException ke) {
                    // Treat a Kafka exception as unrecoverable
                    // stop this task and queue a new one
                    ok = false;
                    executor.execute(newRunnable());
                }
 */
