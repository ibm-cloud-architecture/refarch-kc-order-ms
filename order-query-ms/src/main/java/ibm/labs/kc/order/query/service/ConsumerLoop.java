package ibm.labs.kc.order.query.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.kafka.common.KafkaException;

import ibm.labs.kc.order.query.kafka.ErrorProducer;
import ibm.labs.kc.order.query.kafka.OrderConsumer;
import ibm.labs.kc.order.query.model.events.ErrorEvent;
import ibm.labs.kc.order.query.model.events.EventEmitter;
import ibm.labs.kc.order.query.model.events.EventListener;
import ibm.labs.kc.order.query.model.events.OrderEvent;

@WebListener
public class ConsumerLoop implements ServletContextListener {
    static final Logger logger = Logger.getLogger(ConsumerLoop.class.getName());

    private OrderConsumer consumer;
    private EventListener listener;
    private EventEmitter emitter;
    private boolean running = true;

    public ConsumerLoop() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("ConsumerLoop contextInitialized");

        // Perform action during application's startup
        listener = new QueryService();
        consumer = new OrderConsumer();
        emitter = new ErrorProducer();
        new Thread() {
            public void run() {
                logger.info("ConsumerLoop thread started");
                while (running) {
                    try {
                        List<OrderEvent> events = consumer.poll();
                        for (OrderEvent event : events) {
                            try {
                                listener.handle(event);
                            } catch (Exception e) {
                                ErrorEvent errorEvent = new ErrorEvent(System.currentTimeMillis(), ErrorEvent.TYPE_ERROR, "1", event, e.getMessage());
                                try {
                                    emitter.emit(errorEvent);
                                } catch (Exception e1) {
                                    logger.log(Level.SEVERE, "Failed emitting Error event " + errorEvent, e1);
                                }
                            }
                        }
                    } catch (KafkaException ke) {
                        //TODO
                    }
                }
                consumer.close();
            };
        }.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("ConsumerLoop contextDestroyed");

        // Perform action during application's shutdown
        running = false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}