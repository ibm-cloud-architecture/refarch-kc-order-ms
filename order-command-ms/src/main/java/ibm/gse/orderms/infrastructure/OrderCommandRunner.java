package ibm.gse.orderms.infrastructure;

import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try {
            while (running && orderCommandAgent.isRunning()) {
                try {
                    // poll for orderCommand events from the order-commands topic
                    orderCommandAgent.poll();
                } catch (KafkaException ke) {
                    // Treat a Kafka exception as unrecoverable
                    // stop this task and queue a new one
                    running = false;
                }
            }
        } finally {
        	orderCommandAgent.safeClose();  
        }
	}

}

