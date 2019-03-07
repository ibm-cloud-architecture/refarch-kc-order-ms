package ibm.labs.kc.order.query.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComplexQueryDAOImpl implements ComplexQueryDAO{
	
	private static final Logger logger = LoggerFactory.getLogger(ComplexQueryDAOImpl.class);
	
	private final Map<String, ComplexQueryOrder> orderEvents;
	private ArrayList<ComplexQueryOrder> orderHistory = new ArrayList<>();
	private ArrayList<ComplexQueryOrder> ordHistory;
	
	private static ComplexQueryDAOImpl instance;

    public synchronized static ComplexQueryDAO instance() {
        if (instance == null) {
            instance = new ComplexQueryDAOImpl();
        }
        return instance;
    }

    // for testing
    public ComplexQueryDAOImpl() {
    	orderEvents = new ConcurrentHashMap<>();
    	ordHistory = new ArrayList<>();
    }

	@Override
	public void add(ComplexQueryOrder complexQueryOrder) {
		logger.info("Adding order event " + complexQueryOrder.getOrderID());
        if (orderEvents.putIfAbsent(complexQueryOrder.getOrderID(), complexQueryOrder) != null) {
            throw new IllegalStateException("Order Event already exists " + complexQueryOrder.getOrderID());
        }	
	}

	@Override
	public void update(ComplexQueryOrder complexQueryOrder) {
		logger.info("Updating order id " + complexQueryOrder.getOrderID());
        if (orderEvents.replace(complexQueryOrder.getOrderID(), complexQueryOrder) == null) {
            throw new IllegalStateException("Order does not exist " + complexQueryOrder.getOrderID());
        }	
	}

	@Override
	public void orderHistory(ComplexQueryOrder complexQueryOrder) {

		ComplexQueryOrder newComplexQueryOrder = orderEvents.get(complexQueryOrder.getOrderID());
		
		ComplexQueryOrder modifiedComplexQueryOrder = new ComplexQueryOrder(newComplexQueryOrder.getOrderID(),
																			newComplexQueryOrder.getTimestampMillis(),
																			newComplexQueryOrder.getAction(),
																			newComplexQueryOrder.getType());
		
		logger.info("Adding to order events history " + modifiedComplexQueryOrder.getOrderID() + modifiedComplexQueryOrder.getTimestampMillis()+
				modifiedComplexQueryOrder.getAction(),modifiedComplexQueryOrder.getType());
		
		orderHistory.add(modifiedComplexQueryOrder);
		ordHistory.add(modifiedComplexQueryOrder);
    		
	}

	@Override
	public Collection<ComplexQueryOrder> getOrderStatus(String orderID) {
		Collection<ComplexQueryOrder> result = new ArrayList<>();
        for (ComplexQueryOrder complexQueryOrder : orderHistory) {
            if (orderID.equals(complexQueryOrder.getOrderID())) {
            	logger.info("Getting order status "+complexQueryOrder.getTimestampMillis() + complexQueryOrder.getAction() + complexQueryOrder.getType());
            	ComplexQueryOrder reqOrder = new ComplexQueryOrder(complexQueryOrder.getTimestampMillis(), complexQueryOrder.getAction(), complexQueryOrder.getType());
                result.add(reqOrder);
            }
        }
        return Collections.unmodifiableCollection(result);
	}

}
