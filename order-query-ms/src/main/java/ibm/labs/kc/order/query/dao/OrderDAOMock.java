package ibm.labs.kc.order.query.dao;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ibm.labs.kc.order.query.model.Order;


public class OrderDAOMock implements OrderDAO {

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
        orders.put(o.getOrderID(), o);
    }

}
