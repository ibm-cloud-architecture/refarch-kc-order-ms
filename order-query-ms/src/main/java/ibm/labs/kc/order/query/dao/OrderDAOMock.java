package ibm.labs.kc.order.query.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OrderDAOMock implements OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAOMock.class);

    private final Map<String, QueryOrder> orders;
    private ArrayList<QueryOrder> ordHistory;
    private ArrayList<QueryOrder> orderHistory = new ArrayList<>();

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
        ordHistory = new ArrayList<>();
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
        if (orders.replace(order.getOrderID(), order) == null) {
            throw new IllegalStateException("order does not already exist " + order.getOrderID());
        }
    }
    
    @Override
	public void orderHistory(QueryOrder o) {
    	
    	QueryOrder ord = orders.get(o.getOrderID()); 
    	
    	if(o.getVoyageID() == null) {
    		QueryOrder modifiedOrder = new QueryOrder(ord.getOrderID(),ord.getProductID(),
    				ord.getCustomerID(), ord.getQuantity(), ord.getPickupAddress(),
    				ord.getPickupDate(), ord.getDestinationAddress(), ord.getExpectedDeliveryDate(),
    				ord.getStatus());
    		logger.info("Adding to order history " + ord.getOrderID() + ord.getStatus()+ ord.getCustomerID());
    		orderHistory.add(modifiedOrder);
    		ordHistory.add(modifiedOrder);
    	}
    	else if(o.getContainerID() == null){
    		QueryOrder modifiedOrder = new QueryOrder(ord.getOrderID(),ord.getProductID(),
    				ord.getCustomerID(), ord.getQuantity(), ord.getPickupAddress(),
    				ord.getPickupDate(), ord.getDestinationAddress(), ord.getExpectedDeliveryDate(),
    				ord.getStatus(), ord.getVoyageID());
    		logger.info("Adding to order history " + ord.getOrderID() + ord.getStatus()+ ord.getCustomerID()+ ord.getVoyageID());
    		orderHistory.add(modifiedOrder);
    		ordHistory.add(modifiedOrder);
    	}
    	else{
    		QueryOrder modifiedOrder = new QueryOrder(ord.getOrderID(),ord.getProductID(),
    				ord.getCustomerID(), ord.getQuantity(), ord.getPickupAddress(),
    				ord.getPickupDate(), ord.getDestinationAddress(), ord.getExpectedDeliveryDate(),
    				ord.getStatus(), ord.getVoyageID(), ord.getContainerID());
    		logger.info("Adding to order history " + ord.getOrderID() + ord.getStatus()+ ord.getCustomerID()+ ord.getVoyageID()+ ord.getContainerID());
    		orderHistory.add(modifiedOrder);
    		ordHistory.add(modifiedOrder);
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
        	logger.info("Getting by status "+order.getCustomerID()+" "+order.getOrderID()+" "+order.getStatus());
            if (status.equals(order.getStatus())) {
                result.add(order);
            }
        }
        return Collections.unmodifiableCollection(result);
    }
    
    @Override
    public Collection<QueryOrder> getOrderStatus(String orderID, String customerID) {
        Collection<QueryOrder> result = new ArrayList<>();
        for (QueryOrder order : orderHistory) {
            if (orderID.equals(order.getOrderID())) {
            	logger.info("Getting order status "+order.getOrderID()+order.getStatus());
                result.add(order);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

}
