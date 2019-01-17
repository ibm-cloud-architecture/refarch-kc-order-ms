package ibm.labs.kc.order.query.dao;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ibm.labs.kc.order.query.model.Order;


public class OrderDAOMock implements OrderDAO {
    static final Logger logger = Logger.getLogger(OrderDAOMock.class.getName());

    private final Map<String, Order> orders;

    private static OrderDAOMock instance;
    
    public synchronized static OrderDAO instance() {
        if (instance == null) {
            instance = new OrderDAOMock();
        }
        return instance;
    }

    public OrderDAOMock() {
        orders = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<Order> getById(String orderId) {
        Order o = orders.get(orderId);
        return Optional.ofNullable(o);
    }

    @Override
    public void upsert(Order o) {
        System.out.println("upsert order " + o);
        try {
            orders.put(o.getOrderId(), o);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}
