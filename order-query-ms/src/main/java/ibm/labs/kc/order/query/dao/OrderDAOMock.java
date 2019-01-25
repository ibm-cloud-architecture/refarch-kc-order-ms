package ibm.labs.kc.order.query.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ibm.labs.kc.order.query.model.QueryOrder;


public class OrderDAOMock implements OrderDAO {
    static final Logger logger = Logger.getLogger(OrderDAOMock.class.getName());

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

    @Override
    public Optional<QueryOrder> getById(String orderId) {
        QueryOrder o = orders.get(orderId);
        return Optional.ofNullable(o);
    }

    @Override
    public void add(QueryOrder o) {
        logger.info("Adding order id " + o.getOrderID());
        try {
            orders.put(o.getOrderID(), o);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void update(QueryOrder order) {
        if (orders.replace(order.getOrderID(), order) == null) {
            throw new IllegalStateException("order does not already exist");
        }
    }

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

    @Override
    public Collection<QueryOrder> getByStatus(String status) {
        Collection<QueryOrder> result = new ArrayList<>();

        for (QueryOrder order : orders.values()) {
            if (Objects.equals(status, order.getStatus())) {
                result.add(order);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

}
