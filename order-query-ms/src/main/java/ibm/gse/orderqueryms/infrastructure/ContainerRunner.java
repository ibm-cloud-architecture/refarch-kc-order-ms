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
		
		//NEW
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
		
		/*
		//OLD 		
		logger.info("ConsumerLoop thread started");
        EventListener orderActionServicelistener = new OrderActionService();
        EventEmitter emitter = new ErrorProducer();
        boolean ok = true;
        try {
            while (running && ok) {
                try {
                    List<ContainerEvent> contEvents = containerConsumer.poll();
                    for (ContainerEvent event : contEvents) {
                        try {
                        	orderActionServicelistener.handle(event, "container");
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
                    containerExecutor.execute(newContainerRunnable());
                    // TODO: after a number of retrying, mark app as unhealthy for external monitoring restart
                }
            }
        } finally {
        	containerConsumer.safeClose();
            emitter.safeClose();
        }
        */

	}

}
