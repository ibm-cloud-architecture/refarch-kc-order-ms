package ibm.labs.kc.order.command.service;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import ibm.labs.kc.order.command.kafka.OrderConsumer;
import ibm.labs.kc.order.command.model.EventListener;
import ibm.labs.kc.order.command.model.OrderEvent;

@WebListener
public class ConsumerLoop implements ServletContextListener{
    private static final Logger logger = Logger.getLogger(ConsumerLoop.class.getName());

    private OrderConsumer consumer;
    private volatile boolean running = true;
    private EventListener listener;

    public ConsumerLoop() {
        listener = OrderAdminService.instance();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("ConsumerLoop contextInitialized");

        consumer = OrderConsumer.instance();
        new Thread() {
            public void run() {
                logger.info("ConsumerLoop thread started");
                while (running) {
                    List<OrderEvent> orderEvents = consumer.poll();
                    for (OrderEvent event : orderEvents) {
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

        running = false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}