package ibm.gse.orderqueryms.domain.service;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderqueryms.app.AppRegistry;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistory;
import ibm.gse.orderqueryms.infrastructure.repository.OrderHistoryDAO;

public class OrderHistoryService {

	static final Logger logger = LoggerFactory.getLogger(OrderHistoryService.class);

    private OrderHistoryDAO orderHistoryDAO;

    public OrderHistoryService() {
        orderHistoryDAO = AppRegistry.getInstance().orderHistoryRepository();
    }
    
    public Collection<OrderHistory> getOrderStatus(String orderId){
    	logger.info("OrderHistoryService:getOrderStatus searching for orderId (" + orderId + ")");
    	Collection<OrderHistory> results = new ArrayList<OrderHistory>();
    	
    	try {
    		results = orderHistoryDAO.getOrderStatus(orderId);
    		logger.info("OrderHistoryService:getOrderStatus found " + results.size() + " status for orderId (" + orderId + ")");
    	} catch (Exception e) {
    		e.printStackTrace();
    		logger.error("OrderHistoryService:getOrderStatus encountered an error for orderId ("+orderId+")", e);
    	}
   	
    	return results;
    }
	
}
