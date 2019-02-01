package ibm.labs.kc.order.command.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderDAOMock implements OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAOMock.class);
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
        logger.info("Adding order id " + order.getOrderID());
        if (orders.putIfAbsent(order.getOrderID(), order) != null) {
            throw new IllegalStateException("order already exists " + order.getOrderID());
        }
    }

    @Override
    public Collection<CommandOrder> getAll() {
        return Collections.unmodifiableCollection(orders.values());
    }

    @Override
    public void update(CommandOrder order) {
        logger.info("Updating order id " + order.getOrderID());
        if (orders.replace(order.getOrderID(), order) == null) {
            throw new IllegalStateException("order does not already exist " + order.getOrderID());
        }
    }

    @Override
    public Optional<CommandOrder> getByID(String orderId) {
        CommandOrder o = orders.get(orderId);
        return Optional.ofNullable(o);
    }

}
