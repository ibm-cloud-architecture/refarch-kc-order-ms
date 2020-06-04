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
