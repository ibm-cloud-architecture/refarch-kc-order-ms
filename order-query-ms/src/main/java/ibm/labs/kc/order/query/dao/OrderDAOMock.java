package ibm.labs.kc.order.query.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OrderDAOMock implements OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAOMock.class);

    private final Map<String, QueryOrder> orders;
    private final Collection<QueryOrder> orderHistory;

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
        orderHistory = new ArrayList<>();
    }

    @Override
    public Optional<QueryOrder> getById(String orderId) {
        QueryOrder o = orders.get(orderId);
        return Optional.ofNullable(o);
    }

    @Override
    public void add(QueryOrder order) {
        logger.info("Adding order id " + order.getOrderID());
        if (orders.putIfAbsent(order.getOrderID(), order) != null) {
            throw new IllegalStateException("order already exists " + order.getOrderID());
        }
    }

    @Override
    public void update(QueryOrder order) {
    	logger.info("Updating order id " + order.getOrderID());
    	QueryOrder ord = new QueryOrder(orders.get(order.getOrderID()).getOrderID(),orders.get(order.getOrderID()).getProductID(),
    			orders.get(order.getOrderID()).getCustomerID(), orders.get(order.getOrderID()).getQuantity(), orders.get(order.getOrderID()).getPickupAddress(),
    			orders.get(order.getOrderID()).getPickupDate(), orders.get(order.getOrderID()).getDestinationAddress(), orders.get(order.getOrderID()).getExpectedDeliveryDate(),
    			orders.get(order.getOrderID()).getStatus());
    	orderHistory.add(ord);
    	for(QueryOrder o: orderHistory){
    		System.out.println(o.getOrderID()+" "+o.getStatus());
    	}
        if (orders.replace(order.getOrderID(), order) == null) {
            throw new IllegalStateException("order does not already exist " + order.getOrderID());
        }
    }
    
    @Override
	public void orderHistory(QueryOrder o) {
		// TODO Auto-generated method stub
    	logger.info("Adding to order history " + o.getOrderID() + o.getStatus()+ o.getCustomerID());
    	orderHistory.add(o);
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
    
    @Override
    public Collection<QueryOrder> getOrderStatus(String orderID, String customerID) {
    	System.out.println("I am in order status");
        Collection<QueryOrder> result = new ArrayList<>();
        for (QueryOrder order : orderHistory) {
        	System.out.println(order.getCustomerID()+" "+order.getOrderID()+" "+order.getStatus());
            if (Objects.equals(orderID, order.getOrderID()) && Objects.equals(customerID, order.getCustomerID())) {
            	System.out.println("got it"+order.getOrderID()+order.getStatus());
                result.add(order);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

}
