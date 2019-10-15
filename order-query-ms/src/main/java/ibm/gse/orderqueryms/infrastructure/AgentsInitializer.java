package ibm.gse.orderqueryms.infrastructure;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderqueryms.infrastructure.kafka.ApplicationConfig;

@WebListener
public class AgentsInitializer implements ServletContextListener {
	static final Logger logger = LoggerFactory.getLogger(AgentsInitializer.class);
    
    private OrderRunner orderRunner;
    private ContainerRunner containerRunner;
    private ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("ConsumerLoop contextInitialized");

        executor = Executors.newFixedThreadPool(2);
        orderRunner = new OrderRunner();
        containerRunner = new ContainerRunner();
        
        executor.execute(orderRunner);
        executor.execute(containerRunner);
    }

    /*
    private Runnable newReloadOrderRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                logger.info("ReloadState started");
                EventListener queryServiceListener = new QueryService();
                EventListener orderActionServicelistener = new OrderActionService();

                while (!orderConsumer.reloadCompleted()) {
                    List<OrderEvent> events = orderConsumer.pollForReload();
                    for (OrderEvent event : events) {
                        try {
                        	queryServiceListener.handle(event, "order");
                        	orderActionServicelistener.handle(event, "order");
                        } catch (Exception e) {
                            e.printStackTrace();
                            // TODO fail to restart would be the correct handling
                            // mark the app as unhealthy
                        }
                    }
                }
                
                logger.info("ReloadState order completed");
                orderConsumer.safeReloadClose();
                
            }
        };
    }
    
    private Runnable newReloadContainerRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                logger.info("ReloadState started");
                EventListener orderActionServicelistener = new OrderActionService();

                while (!containerConsumer.reloadCompleted()) {
                    List<ContainerEvent> events = containerConsumer.pollForReload();
                    for (ContainerEvent event : events) {
                        try {
                        	orderActionServicelistener.handle(event, "container");
                        } catch (Exception e) {
                            e.printStackTrace();
                            // TODO fail to restart would be the correct handling
                            // mark the app as unhealthy
                        }
                    }
                }
                logger.info("ReloadState completed");
                containerConsumer.safeReloadClose();
            }
        };
    }
	*/


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("ConsumerLoop contextDestroyed");
        orderRunner.stop();
        containerRunner.stop();
        executor.shutdownNow();
        try {
        	executor.awaitTermination(ApplicationConfig.TERMINATION_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            logger.warn("awaitTermination( interrupted", ie);
        }
    }
}
