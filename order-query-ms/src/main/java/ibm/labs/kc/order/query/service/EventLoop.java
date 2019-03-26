package ibm.labs.kc.order.query.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.labs.kc.order.query.action.OrderActionService;
import ibm.labs.kc.order.query.kafka.ApplicationConfig;
import ibm.labs.kc.order.query.kafka.ContainerConsumer;
import ibm.labs.kc.order.query.kafka.ErrorProducer;
import ibm.labs.kc.order.query.kafka.OrderConsumer;
import ibm.labs.kc.order.query.model.events.ContainerEvent;
import ibm.labs.kc.order.query.model.events.ErrorEvent;
import ibm.labs.kc.order.query.model.events.EventEmitter;
import ibm.labs.kc.order.query.model.events.EventListener;
import ibm.labs.kc.order.query.model.events.OrderEvent;

@WebListener
public class EventLoop implements ServletContextListener {
	static final Logger logger = LoggerFactory.getLogger(EventLoop.class);

    private boolean running = true;
    private OrderConsumer orderConsumer;
    private ContainerConsumer containerConsumer;
    private ExecutorService orderExecutor;
    private ExecutorService containerExecutor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("ConsumerLoop contextInitialized");

        orderConsumer = new OrderConsumer();
        orderExecutor = Executors.newFixedThreadPool(1);
        orderExecutor.execute(newReloadOrderRunnable());
        orderExecutor.execute(newOrderRunnable());
        
        containerConsumer = new ContainerConsumer();
        containerExecutor = Executors.newFixedThreadPool(1);
        containerExecutor.execute(newReloadContainerRunnable());
        containerExecutor.execute(newContainerRunnable());
    }

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

    private Runnable newOrderRunnable() {
        return new Runnable() {
            @Override
            public void run() {
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
            };
        };
    }
    
    private Runnable newContainerRunnable() {
        return new Runnable() {
            @Override
            public void run() {
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
            };
        };
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("ConsumerLoop contextDestroyed");
        running = false;
        orderExecutor.shutdownNow();
        try {
        	orderExecutor.awaitTermination(ApplicationConfig.TERMINATION_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            logger.warn("awaitTermination( interrupted", ie);
        }
        containerExecutor.shutdownNow();
        try {
        	containerExecutor.awaitTermination(ApplicationConfig.TERMINATION_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            logger.warn("awaitTermination( interrupted", ie);
        }
    }
}
