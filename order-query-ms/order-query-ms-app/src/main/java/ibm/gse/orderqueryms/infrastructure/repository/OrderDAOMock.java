package ibm.gse.orderqueryms.infrastructure.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderqueryms.domain.model.order.QueryOrder;


public class OrderDAOMock implements OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAOMock.class);

    private final Map<String, QueryOrder> orders;

    private static OrderDAOMock instance;

    public synchronized static OrderDAO instance() {
        if (instance == null) {
            instance = new OrderDAOMock();
        }
        return instance;
    }

    // for testing
    public OrderDAOMock() {
        orders = new ConcurrentHashMap<>();
    }

    // Getting the order based on orderID
    @Override
    public Optional<QueryOrder> getById(String orderId) {
        QueryOrder o = orders.get(orderId);
        return Optional.ofNullable(o);
    }

    // Storing the orders
    @Override
    public void add(QueryOrder order) {
        logger.info("Adding order id " + order.getOrderID());
        if (orders.putIfAbsent(order.getOrderID(), order) != null) {
            throw new IllegalStateException("order already exists " + order.getOrderID());
        }
    }

    // Updating the orders based on the recent status
    @Override
    public void update(QueryOrder order) {
    	logger.info("Updating order id " + order.getOrderID());
        if (orders.replace(order.getOrderID(), order) == null) {
            throw new IllegalStateException("order does not already exist " + order.getOrderID());
        }
    }

    // Getting the order based on the Manufacturer
    @Override
    public Collection<QueryOrder> getByManuf(String manuf) {
        // DEMO: check manuf against customerID
        Collection<QueryOrder> result = new ArrayList<>();

        // It's safe to iterate over the values even if modified concurrently
        for (QueryOrder order : orders.values()) {
            if (Objects.equals(manuf, order.getCustomerID())) {
                result.add(order);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    // Getting the order based on the status
    @Override
    public Collection<QueryOrder> getByStatus(String status) {
        Collection<QueryOrder> result = new ArrayList<>();

        for (QueryOrder order : orders.values()) {
        	logger.info("Getting by status "+order.getCustomerID()+" "+order.getOrderID()+" "+order.getStatus());
            if (status.equals(order.getStatus())) {
                result.add(order);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    // Getting the order based on the containerId
    @Override
    public Collection<QueryOrder> getByContainerId(String containerId) {
        Collection<QueryOrder> result = new ArrayList<>();

        for (QueryOrder order : orders.values()) {
            if (Objects.equals(containerId, order.getContainerID())) {
                result.add(order);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    // Getting the order based on the containerId
    @Override
    public Collection<QueryOrder> getOrders() {
        Collection<QueryOrder> result = new ArrayList<>();

        for (QueryOrder order : orders.values()) {
            result.add(order);
        }
        return Collections.unmodifiableCollection(result);
    }

}
