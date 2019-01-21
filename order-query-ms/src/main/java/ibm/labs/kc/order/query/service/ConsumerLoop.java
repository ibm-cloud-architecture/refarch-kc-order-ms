package ibm.labs.kc.order.query.service;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import ibm.labs.kc.order.query.kafka.OrderConsumer;
import ibm.labs.kc.order.query.model.EventListener;
import ibm.labs.kc.order.query.model.OrderEvent;

@WebListener
public class ConsumerLoop implements ServletContextListener {
    static final Logger logger = Logger.getLogger(ConsumerLoop.class.getName());

    private OrderConsumer consumer;
    private EventListener listener;
    private boolean running = true;

    public ConsumerLoop() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("ConsumerLoop contextInitialized");
        new Exception(""+sce+" "+System.currentTimeMillis()).printStackTrace();

        // Perform action during application's startup
        listener = QueryService.instance();
        consumer = OrderConsumer.instance();
        new Thread() {
            public void run() {
                logger.info("ConsumerLoop thread started");
                while (running) {
                    List<OrderEvent> events = consumer.poll();
                    for (OrderEvent event : events) {
                        listener.handle(event);
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