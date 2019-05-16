package ibm.labs.kc.order.command.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.labs.kc.order.command.kafka.ApplicationConfig;
import ibm.labs.kc.order.command.kafka.ErrorProducer;
import ibm.labs.kc.order.command.kafka.OrderConsumer;
import ibm.labs.kc.order.command.model.events.ErrorEvent;
import ibm.labs.kc.order.command.model.events.EventEmitter;
import ibm.labs.kc.order.command.model.events.EventListener;
import ibm.labs.kc.order.command.model.events.OrderEvent;

@WebListener
public class EventLoop implements ServletContextListener{
    private static final Logger logger = LoggerFactory.getLogger(EventLoop.class);

    private volatile boolean running = true;
    private OrderConsumer consumer;
    private ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("@@@ Order Consumer Loop contextInitialized v0.0.4");

        consumer = new OrderConsumer();
        executor = Executors.newFixedThreadPool(1);
        executor.execute(newReloadRunnable());
        executor.execute(newRunnable());
    }

    private Runnable newReloadRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                logger.info("Reload Order State started");
                EventListener listener = new OrderAdminService();

                while (!consumer.reloadCompleted()) {
                    List<OrderEvent> events = consumer.pollForReload();
                    for (OrderEvent event : events) {
                        try {
                            listener.handle(event);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // TODO fail to restart would be the correct handling
                            // mark the app as unhealthy
                        }
                    }
                }
                logger.info("ReloadState completed");
                consumer.safeReloadClose();
            }
        };
    }

    private Runnable newRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                logger.info("ConsumerLoop thread started");
                EventListener listener = new OrderAdminService();
                EventEmitter emitter = new ErrorProducer();
                boolean ok = true;
                try {
                    while (running && ok) {
                        try {
                            List<OrderEvent> events = consumer.poll();
                            for (OrderEvent event : events) {
                                try {
                                    listener.handle(event);
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
                    }
                } finally {
                    consumer.safeClose();
                    emitter.safeClose();
                }
            };
        };
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Order ConsumerLoop contextDestroyed");
        running = false;
        executor.shutdownNow();
        try {
            executor.awaitTermination(ApplicationConfig.TERMINATION_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            logger.warn("awaitTermination( interrupted", ie);
        }
    }

}