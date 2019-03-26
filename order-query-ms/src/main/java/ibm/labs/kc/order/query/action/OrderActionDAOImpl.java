package ibm.labs.kc.order.query.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderActionDAOImpl implements OrderActionDAO{
	
	private static final Logger logger = LoggerFactory.getLogger(OrderActionDAOImpl.class);
	
	private final Map<String, OrderAction> orderEvents;
	private final Map<String, OrderAction> containerEvents;
	private ArrayList<OrderAction> orderHistory = new ArrayList<>();
	private ArrayList<OrderAction> containerHistory = new ArrayList<>();
	private ArrayList<OrderAction> ordHistory;
	private ArrayList<OrderAction> contHistory;
	
	private static OrderActionDAOImpl instance;

    public synchronized static OrderActionDAO instance() {
        if (instance == null) {
            instance = new OrderActionDAOImpl();
        }
        return instance;
    }

    // for testing
    public OrderActionDAOImpl() {
    	
    	orderEvents = new ConcurrentHashMap<>();
    	ordHistory = new ArrayList<>();
    	
    	containerEvents = new ConcurrentHashMap<>();
    	contHistory = new ArrayList<>();
    	
    }

    // Storing the order events
	@Override
	public void addOrder(OrderAction orderAction) {
		logger.info("Adding order event " + orderAction.getOrderActionItem().getOrderID());
        if (orderEvents.putIfAbsent(orderAction.getOrderActionItem().getOrderID(), orderAction) != null) {
            throw new IllegalStateException("Order Event already exists " + orderAction.getOrderActionItem().getOrderID());
        }	
	}
	
	// Storing the container events
	@Override
	public void addContainer(OrderAction orderAction) {
		logger.info("Adding container event " + orderAction.getOrderActionItem().getContainerID());
        if (containerEvents.putIfAbsent(orderAction.getOrderActionItem().getContainerID(), orderAction) != null) {
            throw new IllegalStateException("Container Event already exists " + orderAction.getOrderActionItem().getContainerID());
        }	
		
	}

	// Updating the stored order events based on the recent status
	@Override
	public void updateOrder(OrderAction orderAction) {
		logger.info("Updating order id " + orderAction.getOrderActionItem().getOrderID());
        if (orderEvents.replace(orderAction.getOrderActionItem().getOrderID(), orderAction) == null) {
            throw new IllegalStateException("Order does not exist " + orderAction.getOrderActionItem().getOrderID());
        }	
	}
	
	// Updating the stored container events based on the recent status
	@Override
	public void updateContainer(OrderAction orderAction) {
		logger.info("Updating container id " + orderAction.getOrderActionItem().getContainerID());
        if (containerEvents.replace(orderAction.getOrderActionItem().getContainerID(), orderAction) == null) {
            throw new IllegalStateException("Container does not exist " + orderAction.getOrderActionItem().getContainerID());
        }
	}
	
	// Getting the event by order Id
	@Override
	public Optional<OrderActionInfo> getByOrderId(String orderId) {
		OrderActionInfo o = orderEvents.get(orderId).getOrderActionItem();
        return Optional.ofNullable(o);
	}
	
	// Getting the event by container Id
	@Override
	public Optional<OrderActionInfo> getByContainerId(String containerId) {
		OrderActionInfo o = containerEvents.get(containerId).getOrderActionItem();
        return Optional.ofNullable(o);
	}

	// Maintaining the history of all order events that happened so far
	@Override
	public void orderHistory(OrderAction orderAction) {

		OrderAction newOrderAction = orderEvents.get(orderAction.getOrderActionItem().getOrderID());
		
		OrderAction modifiedOrderAction = new OrderAction(newOrderAction.getOrderActionItem(),
																newOrderAction.getTimestampMillis(),
																newOrderAction.getAction(),
																newOrderAction.getType());
		
		logger.info("Adding to order events history " + modifiedOrderAction.getOrderActionItem().getOrderID() + modifiedOrderAction.getTimestampMillis()+
				modifiedOrderAction.getAction(),modifiedOrderAction.getType());
		
		orderHistory.add(modifiedOrderAction);
		ordHistory.add(modifiedOrderAction);
    		
	}
	
	// Maintaining the history of all container events that happened so far
	@Override
	public void containerHistory(OrderAction orderAction) {
		
		OrderAction newOrderAction = containerEvents.get(orderAction.getOrderActionItem().getContainerID());
		
		OrderAction modifiedOrderAction = new OrderAction(newOrderAction.getOrderActionItem(),
																newOrderAction.getTimestampMillis(),
																newOrderAction.getAction(),
																newOrderAction.getType());
		
		logger.info("Adding to container events history " + modifiedOrderAction.getOrderActionItem().getContainerID() + modifiedOrderAction.getTimestampMillis()+
				modifiedOrderAction.getAction(),modifiedOrderAction.getType());
		
		containerHistory.add(modifiedOrderAction);
		contHistory.add(modifiedOrderAction);
		
	}

	// Getting the history based on the orderID
	@Override
	public Collection<OrderAction> getOrderStatus(String orderID) {
		ArrayList<OrderAction> result = new ArrayList<>();
        for (OrderAction orderAction : orderHistory) {
            if (orderID.equals(orderAction.getOrderActionItem().getOrderID())) {
            	logger.info("Getting order status "+orderAction.getTimestampMillis() + orderAction.getAction() + orderAction.getType());
            	OrderAction reqOrder = new OrderAction(orderAction.getTimestampMillis(), orderAction.getAction(), orderAction.getType());
                result.add(reqOrder);
                if(orderAction.getAction().equals("ContainerAllocated")){
                	String containerID = orderAction.getOrderActionItem().getContainerID();
                	result.addAll(getContainerforOrder(containerID));
                }
            }
        }
//        Comparator<OrderAction> compareByTimeStamp = (OrderAction oa1, OrderAction oa2) ->
//        Long.compare(oa1.getTimestampMillis(), oa2.getTimestampMillis());
//        Collections.sort(result, compareByTimeStamp);
        return Collections.unmodifiableCollection(result);
	}
	
	// Getting the container details based on the containerID
	public ArrayList<OrderAction> getContainerforOrder(String containerID){
		ArrayList<OrderAction> result = new ArrayList<>();
		for(OrderAction oa: containerHistory){
			if (containerID.equals(oa.getOrderActionItem().getContainerID())) {
				logger.info("Getting container status "+oa.getTimestampMillis() + oa.getAction() + oa.getType());
				OrderAction reqContainer = new OrderAction(oa.getTimestampMillis(), oa.getAction(), oa.getType());
	            result.add(reqContainer);
			}
    	}
		return result;
	}

}

