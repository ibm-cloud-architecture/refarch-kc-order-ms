package ibm.gse.orderms.infrastructure.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderms.domain.model.order.ShippingOrder;

public class OrderRepositoryMock implements OrderRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderRepositoryMock.class);
    private final Map<String, ShippingOrder> orders;

    private static OrderRepositoryMock instance;

    public synchronized static OrderRepository instance() {
        if (instance == null) {
            instance = new OrderRepositoryMock();
        }
        return instance;
    }

    public OrderRepositoryMock() {
        orders = new ConcurrentHashMap<>();
    }

    @Override
    public void addNewShippingOrder(ShippingOrder order) {
        logger.info("Adding order id " + order.getOrderID());
        if (orders.putIfAbsent(order.getOrderID(), order) != null) {
            throw new IllegalStateException("order already exists " + order.getOrderID());
        }
    }

    @Override
    public Collection<ShippingOrder> getAll() {
        return Collections.unmodifiableCollection(orders.values());
    }

    @Override
    public void update(ShippingOrder order) {
        logger.info("Updating order id " + order.getOrderID());
        if (orders.replace(order.getOrderID(), order) == null) {
            throw new IllegalStateException("order does not already exist " + order.getOrderID());
        }
    }

    @Override
    public Optional<ShippingOrder> getByID(String orderId) {
    	ShippingOrder o = orders.get(orderId);
        return Optional.ofNullable(o);
    }

}
