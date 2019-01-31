package ibm.labs.kc.order.command.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OrderDAOMock implements OrderDAO {

    private final Map<String, CommandOrder> orders;

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
    public void add(CommandOrder order) {
        if (orders.putIfAbsent(order.getOrderID(), order) != null) {
            throw new IllegalStateException("order already exists");
        }
    }

    @Override
    public Collection<CommandOrder> getAll() {
        return Collections.unmodifiableCollection(orders.values());
    }

    @Override
    public void update(CommandOrder order) {
        if (orders.replace(order.getOrderID(), order) == null) {
            throw new IllegalStateException("order does not already exist");
        }
    }

    @Override
    public Optional<CommandOrder> getByID(String orderId) {
        CommandOrder o = orders.get(orderId);
        return Optional.ofNullable(o);
    }

}
