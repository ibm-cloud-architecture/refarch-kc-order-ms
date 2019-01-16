package ibm.labs.kc.order.command.dao;

import java.util.HashMap;
import java.util.Map;

import ibm.labs.kc.order.command.model.Order;

public class OrderDAOMock implements OrderDAO {
    
    private final Map<String, Order> orders;

    public OrderDAOMock() {
        orders = new HashMap<>();
    }
    

    @Override
    public void add(Order order) {
        Object o = orders.put(order.getOrderID(), order);
        if (o != null) {
            throw new IllegalStateException("order already exists");
        }
    }

}
