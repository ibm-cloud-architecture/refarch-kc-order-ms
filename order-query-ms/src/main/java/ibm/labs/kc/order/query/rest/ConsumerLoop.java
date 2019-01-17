package ibm.labs.kc.order.query.rest;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import ibm.labs.kc.order.query.dao.OrderDAO;
import ibm.labs.kc.order.query.dao.OrderDAOMock;
import ibm.labs.kc.order.query.kafka.OrderConsumer;
import ibm.labs.kc.order.query.model.Order;

@WebListener
public class ConsumerLoop implements ServletContextListener{
    static final Logger logger = Logger.getLogger(ConsumerLoop.class.getName());

    private OrderDAO orderDAO;
    private OrderConsumer consumer;
    private boolean running = true;

    public ConsumerLoop() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("ConsumerLoop contextInitialized");

        // Perform action during application's startup
        orderDAO = OrderDAOMock.instance();

        consumer = OrderConsumer.instance();
        new Thread() {
            public void run() {
                logger.info("ConsumerLoop thread started");
                while (running) {
                    List<Order> orders = consumer.poll();
                    for (Order o : orders) {
                        orderDAO.upsert(o);
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