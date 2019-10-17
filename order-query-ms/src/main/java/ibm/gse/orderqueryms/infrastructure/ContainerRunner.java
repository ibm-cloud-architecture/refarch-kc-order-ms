package ibm.gse.orderqueryms.infrastructure;

import java.util.List;

import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderqueryms.infrastructure.events.container.ContainerEvent;
import ibm.gse.orderqueryms.infrastructure.kafka.ContainerAgent;

public class ContainerRunner implements Runnable {

	static final Logger logger = LoggerFactory.getLogger(ContainerRunner.class);
	
	private volatile boolean running = true;
	
	public ContainerRunner() {
	
	}

	public void stop() {
		this.running = false;
	}
	
	@Override
	public void run() {
		
		logger.info("Container event consumer loop thread started");
		ContainerAgent containerEventAgent = new ContainerAgent();
        boolean ok = true;
        try {
            while (running && ok) {
                try {
                    List<ContainerEvent> events = containerEventAgent.poll();
                    for (ContainerEvent event : events) {
                    	containerEventAgent.handle(event);
                    }
                } catch (KafkaException ke) {
                    // Treat a Kafka exception as unrecoverable
                    // stop this task and queue a new one
                    ok = false;
                }
            }
        } finally {
        	containerEventAgent.safeClose();  
        }

	}

}
