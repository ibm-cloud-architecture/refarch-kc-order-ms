package ibm.labs.kc.order.query.rest;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;

import ibm.labs.kc.order.query.dao.OrderDAO;
import ibm.labs.kc.order.query.dao.OrderDAOMock;
import ibm.labs.kc.order.query.kafka.OrderConsumer;
import ibm.labs.kc.order.query.model.Order;

@ApplicationScoped
public class ConsumerLoop {

//    private OrderDAO orderDAO;
//    private OrderConsumer consumer;
//    private boolean running = true;
//
//    public void init(@Observes @Initialized(ApplicationScoped.class) ServletContext context) {
//        // Perform action during application's startup
//        orderDAO = OrderDAOMock.instance();
//
//        consumer = OrderConsumer.instance();
//        new Thread() {
//            public void run() {
//                while (running) {
//                    List<Order> orders = consumer.poll();
//                    for (Order o : orders) {
//                        orderDAO.upsert(o);
//                    }
//                }
//                consumer.close();
//            };
//        }.start();
//    }
//
//    public void destroy(@Observes @Destroyed(ApplicationScoped.class) ServletContext context) {
//        // Perform action during application's shutdown
//        running = false;
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}