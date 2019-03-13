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

	@Override
	public void add(OrderAction complexQueryOrder) {
		logger.info("Adding order event " + complexQueryOrder.getOrderActionItem().getOrderID());
        if (orderEvents.putIfAbsent(complexQueryOrder.getOrderActionItem().getOrderID(), complexQueryOrder) != null) {
            throw new IllegalStateException("Order Event already exists " + complexQueryOrder.getOrderActionItem().getOrderID());
        }	
	}

	@Override
	public void update(OrderAction complexQueryOrder) {
		logger.info("Updating order id " + complexQueryOrder.getOrderActionItem().getOrderID());
        if (orderEvents.replace(complexQueryOrder.getOrderActionItem().getOrderID(), complexQueryOrder) == null) {
            throw new IllegalStateException("Order does not exist " + complexQueryOrder.getOrderActionItem().getOrderID());
        }	
	}
	
	@Override
	public Optional<OrderActionInfo> getById(String orderId) {
		OrderActionInfo o = orderEvents.get(orderId).getOrderActionItem();
        return Optional.ofNullable(o);
	}

	@Override
	public void orderHistory(OrderAction complexQueryOrder) {

		OrderAction newComplexQueryOrder = orderEvents.get(complexQueryOrder.getOrderActionItem().getOrderID());
		
		OrderAction modifiedComplexQueryOrder = new OrderAction(newComplexQueryOrder.getOrderActionItem(),
																			newComplexQueryOrder.getTimestampMillis(),
																			newComplexQueryOrder.getAction(),
																			newComplexQueryOrder.getType());
		
		logger.info("Adding to order events history " + modifiedComplexQueryOrder.getOrderActionItem().getOrderID() + modifiedComplexQueryOrder.getTimestampMillis()+
				modifiedComplexQueryOrder.getAction(),modifiedComplexQueryOrder.getType());
		
		orderHistory.add(modifiedComplexQueryOrder);
		ordHistory.add(modifiedComplexQueryOrder);
    		
	}

	@Override
	public Collection<OrderAction> getOrderStatus(String orderID) {
		Collection<OrderAction> result = new ArrayList<>();
        for (OrderAction complexQueryOrder : orderHistory) {
            if (orderID.equals(complexQueryOrder.getOrderActionItem().getOrderID())) {
            	logger.info("Getting order status "+complexQueryOrder.getTimestampMillis() + complexQueryOrder.getAction() + complexQueryOrder.getType());
            	OrderAction reqOrder = new OrderAction(complexQueryOrder.getTimestampMillis(), complexQueryOrder.getAction(), complexQueryOrder.getType());
                result.add(reqOrder);
            }
        }
        return Collections.unmodifiableCollection(result);
	}

}

