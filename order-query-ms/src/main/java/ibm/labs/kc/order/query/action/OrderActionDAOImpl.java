package ibm.labs.kc.order.query.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderActionDAOImpl implements OrderActionDAO{
	
	private static final Logger logger = LoggerFactory.getLogger(OrderActionDAOImpl.class);
	
	private final Map<String, OrderAction> orderEvents;
	private ArrayList<OrderAction> orderHistory = new ArrayList<>();
	private ArrayList<OrderAction> ordHistory;
	
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
    }

    // Storing the events
	@Override
	public void add(OrderAction orderAction) {
		logger.info("Adding order event " + orderAction.getOrderActionItem().getOrderID());
        if (orderEvents.putIfAbsent(orderAction.getOrderActionItem().getOrderID(), orderAction) != null) {
            throw new IllegalStateException("Order Event already exists " + orderAction.getOrderActionItem().getOrderID());
        }	
	}

	// Updating the stored events based on the recent status
	@Override
	public void update(OrderAction orderAction) {
		logger.info("Updating order id " + orderAction.getOrderActionItem().getOrderID());
        if (orderEvents.replace(orderAction.getOrderActionItem().getOrderID(), orderAction) == null) {
            throw new IllegalStateException("Order does not exist " + orderAction.getOrderActionItem().getOrderID());
        }	
	}
	
	// Getting the event by Id
	@Override
	public Optional<OrderActionInfo> getById(String orderId) {
		OrderActionInfo o = orderEvents.get(orderId).getOrderActionItem();
        return Optional.ofNullable(o);
	}

	// Maintaining the history of all events that happened so far
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

	// Getting the history based on the orderID
	@Override
	public Collection<OrderAction> getOrderStatus(String orderID) {
		Collection<OrderAction> result = new ArrayList<>();
        for (OrderAction orderAction : orderHistory) {
            if (orderID.equals(orderAction.getOrderActionItem().getOrderID())) {
            	logger.info("Getting order status "+orderAction.getTimestampMillis() + orderAction.getAction() + orderAction.getType());
            	OrderAction reqOrder = new OrderAction(orderAction.getTimestampMillis(), orderAction.getAction(), orderAction.getType());
                result.add(reqOrder);
            }
        }
        return Collections.unmodifiableCollection(result);
	}

}

