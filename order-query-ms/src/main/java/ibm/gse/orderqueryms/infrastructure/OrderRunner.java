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

	}

}
