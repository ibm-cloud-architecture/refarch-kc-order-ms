package ibm.gse.orderms.infrastructure;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderms.infrastructure.kafka.KafkaInfrastructureConfig;

/**
 * Servlet contxt listener to start the different messaging consumers of 
 * the application. As each consumer acts as an agent, continuously listening to
 * events, we need to start them when the encapsulating app / microservice is successfuly
 * started, which is why we have to implement a servlet context listener.
 *  
 * This class needs to start the order command event agent and the order event one.
 * 
 *  When the application stops we need to kill the consumers.
 * @author jeromeboyer
 *
 */
@WebListener
public class AgentsInitializer implements ServletContextListener{
    private static final Logger logger = LoggerFactory.getLogger(AgentsInitializer.class);

    private OrderEventRunner orderEventRunner;
    private OrderCommandRunner orderCommandRunner;
    private ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("@@@ Order Command contextInitialized v0.0.8, start agents");
        executor = Executors.newFixedThreadPool(2);
        orderEventRunner = new OrderEventRunner();
        orderCommandRunner = new OrderCommandRunner();  
        //executor.execute(newReloadRunnable());
        executor.execute(orderCommandRunner);
        executor.execute(orderEventRunner);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info(" context destroyed");
        orderEventRunner.stop();
        orderCommandRunner.stop();
        executor.shutdownNow();
        try {
            executor.awaitTermination(KafkaInfrastructureConfig.TERMINATION_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            logger.warn("awaitTermination( interrupted", ie);
        }
    }

}