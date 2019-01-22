package ibm.labs.kc.order.query.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.kafka.common.KafkaException;

import ibm.labs.kc.order.query.kafka.ApplicationConfig;
import ibm.labs.kc.order.query.kafka.ErrorProducer;
import ibm.labs.kc.order.query.kafka.OrderConsumer;
import ibm.labs.kc.order.query.model.events.ErrorEvent;
import ibm.labs.kc.order.query.model.events.EventEmitter;
import ibm.labs.kc.order.query.model.events.EventListener;
import ibm.labs.kc.order.query.model.events.OrderEvent;

@WebListener
public class EventLoop implements ServletContextListener {
    static final Logger logger = Logger.getLogger(EventLoop.class.getName());

    private boolean running = true;
    private ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("ConsumerLoop contextInitialized");

        executor = Executors.newFixedThreadPool(1);
        executor.execute(newRunnable());
    }

    private Runnable newRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                logger.info("ConsumerLoop thread started");
                EventListener listener = new QueryService();
                OrderConsumer consumer = new OrderConsumer();
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
                                        logger.log(Level.SEVERE, "Failed emitting Error event " + errorEvent, e1);
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
        logger.info("ConsumerLoop contextDestroyed");
        running = false;
        executor.shutdownNow();
        try {
            executor.awaitTermination(ApplicationConfig.TERMINATION_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            logger.log(Level.WARNING, "awaitTermination( interrupted", ie);
        }
    }

}