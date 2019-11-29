package ibm.gse.orderqueryms.infrastructure.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderqueryms.domain.model.order.history.OrderHistory;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistoryInfo;

public class OrderHistoryDAOMock implements OrderHistoryDAO{
	
	private static final Logger logger = LoggerFactory.getLogger(OrderHistoryDAOMock.class);
	
	private final Map<String, OrderHistory> orderEvents;
	private final Map<String, OrderHistory> containerEvents;
	private ArrayList<OrderHistory> orderHistory = new ArrayList<>();
	private ArrayList<OrderHistory> containerHistory = new ArrayList<>();
	private ArrayList<OrderHistory> ordHistory;
	private ArrayList<OrderHistory> contHistory;
	
	private static OrderHistoryDAOMock instance;

    public synchronized static OrderHistoryDAO instance() {
        if (instance == null) {
            instance = new OrderHistoryDAOMock();
        }
        return instance;
    }

    // for testing
    public OrderHistoryDAOMock() {
    	
    	orderEvents = new ConcurrentHashMap<>();
    	ordHistory = new ArrayList<>();
    	
    	containerEvents = new ConcurrentHashMap<>();
    	contHistory = new ArrayList<>();
    	
    }

    // Storing the order events
	@Override
	public void addOrder(OrderHistory orderAction) {
		logger.info("Adding order event " + orderAction.getOrderActionItem().getOrderID());
        if (orderEvents.putIfAbsent(orderAction.getOrderActionItem().getOrderID(), orderAction) != null) {
            throw new IllegalStateException("Order Event already exists " + orderAction.getOrderActionItem().getOrderID());
        }	
	}
	
	// Storing the container events
	@Override
	public void addContainer(OrderHistory orderAction) {
		logger.info("Adding container event " + orderAction.getOrderActionItem().getContainerID());
        if (containerEvents.putIfAbsent(orderAction.getOrderActionItem().getContainerID(), orderAction) != null) {
            throw new IllegalStateException("Container Event already exists " + orderAction.getOrderActionItem().getContainerID());
        }	
		
	}

	// Updating the stored order events based on the recent status
	@Override
	public void updateOrder(OrderHistory orderAction) {
		logger.info("Updating order id " + orderAction.getOrderActionItem().getOrderID());
        if (orderEvents.replace(orderAction.getOrderActionItem().getOrderID(), orderAction) == null) {
            throw new IllegalStateException("Order does not exist " + orderAction.getOrderActionItem().getOrderID());
        }	
	}
	
	// Updating the stored container events based on the recent status
	@Override
	public void updateContainer(OrderHistory orderAction) {
		logger.info("Updating container id " + orderAction.getOrderActionItem().getContainerID());
        if (containerEvents.replace(orderAction.getOrderActionItem().getContainerID(), orderAction) == null) {
            throw new IllegalStateException("Container does not exist " + orderAction.getOrderActionItem().getContainerID());
        }
	}
	
	// Getting the event by order Id
	@Override
	public Optional<OrderHistoryInfo> getByOrderId(String orderId) {
		OrderHistoryInfo o = orderEvents.get(orderId).getOrderActionItem();
        return Optional.ofNullable(o);
	}
	
	// Getting the event by container Id
	@Override
	public Optional<OrderHistoryInfo> getByContainerId(String containerId) {
		OrderHistoryInfo o = containerEvents.get(containerId).getOrderActionItem();
        return Optional.ofNullable(o);
	}

	// Maintaining the history of all order events that happened so far
	@Override
	public void orderHistory(OrderHistory orderAction) {

		OrderHistory newOrderAction = orderEvents.get(orderAction.getOrderActionItem().getOrderID());
		
		OrderHistory modifiedOrderAction = new OrderHistory(newOrderAction.getOrderActionItem(),
																newOrderAction.getTimestampMillis(),
																newOrderAction.getAction(),
																newOrderAction.getType());
		
		logger.info("Adding to order events history " + modifiedOrderAction.getOrderActionItem().getOrderID() + " - " + modifiedOrderAction.getTimestampMillis() + " - " +
				modifiedOrderAction.getAction() + " - " + modifiedOrderAction.getType());
		
		orderHistory.add(modifiedOrderAction);
		ordHistory.add(modifiedOrderAction);
    		
	}
	
	// Maintaining the history of all container events that happened so far
	@Override
	public void containerHistory(OrderHistory orderAction) {
		
		OrderHistory newOrderAction = containerEvents.get(orderAction.getOrderActionItem().getContainerID());
		
		OrderHistory modifiedOrderAction = new OrderHistory(newOrderAction.getOrderActionItem(),
																newOrderAction.getTimestampMillis(),
																newOrderAction.getAction(),
																newOrderAction.getType());
		
		logger.info("Adding to container events history " + modifiedOrderAction.getOrderActionItem().getContainerID() + " - " + modifiedOrderAction.getTimestampMillis() + " - " +
				modifiedOrderAction.getAction() + " - " + modifiedOrderAction.getType());
		
		if(!containerHistory.contains(modifiedOrderAction)){
			containerHistory.add(modifiedOrderAction);
		}
		
		if(!contHistory.contains(modifiedOrderAction)){
			contHistory.add(modifiedOrderAction);
		}
			
	}

	// Getting the history based on the orderID
	@Override
	public Collection<OrderHistory> getOrderStatus(String orderID) {
		ArrayList<OrderHistory> result = new ArrayList<>();
        for (OrderHistory orderAction : orderHistory) {
            if (orderID.equals(orderAction.getOrderActionItem().getOrderID())) {
            	logger.info("Getting order status "+orderAction.getTimestampMillis() + orderAction.getAction() + orderAction.getType());
            	OrderHistory reqOrder = new OrderHistory(orderAction.getTimestampMillis(), orderAction.getAction(), orderAction.getType());
                result.add(reqOrder);
                if(orderAction.getAction().equals("ContainerAllocated")){
                	String containerID = orderAction.getOrderActionItem().getContainerID();
                	result.addAll(getContainerStatusforOrder(containerID));
                }
            }
        }
        Comparator<OrderHistory> compareByTimeStamp = (OrderHistory oa1, OrderHistory oa2) ->
        Long.compare(oa1.getTimestampMillis(), oa2.getTimestampMillis());
        Collections.sort(result, compareByTimeStamp);
        return Collections.unmodifiableCollection(result);
	}
	
	// Getting the container details based on the containerID
	public ArrayList<OrderHistory> getContainerStatusforOrder(String containerID){
		System.out.println("Entered the getcontstatus");
		ArrayList<OrderHistory> result = new ArrayList<>();
		for(OrderHistory oa: containerHistory){
			if (containerID.equals(oa.getOrderActionItem().getContainerID())) {
				logger.info("Getting container status " + oa.getTimestampMillis() + " - " + oa.getAction() + " - " + oa.getType());
				OrderHistory reqContainer = new OrderHistory(oa.getTimestampMillis(), oa.getAction(), oa.getType());
	            result.add(reqContainer);
			}
    	}
		return result;
	}

}

