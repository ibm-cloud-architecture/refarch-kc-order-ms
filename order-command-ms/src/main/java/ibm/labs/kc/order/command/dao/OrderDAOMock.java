package ibm.labs.kc.order.command.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ibm.labs.kc.order.command.model.Order;

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
    public void add(Order order) {
        Object o = orders.put(order.getOrderID(), order);
        if (o != null) {
            throw new IllegalStateException("order already exists");
        }
    }

    @Override
    public Collection<Order> getAll() {
        return Collections.unmodifiableCollection(orders.values());
    }

}
