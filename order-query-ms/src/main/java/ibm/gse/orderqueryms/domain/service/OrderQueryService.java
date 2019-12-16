package ibm.gse.orderqueryms.domain.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderqueryms.app.AppRegistry;
import ibm.gse.orderqueryms.domain.model.order.QueryOrder;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAO;

public class OrderQueryService {
    static final Logger logger = LoggerFactory.getLogger(OrderQueryService.class);

    private OrderDAO orderDAO;

    public OrderQueryService() {
        orderDAO = AppRegistry.getInstance().orderRepository();
    }
	
    public Optional<QueryOrder> getOrderById(String orderId){
    	logger.info("OrderQueryService:getOrderById searching for orderId (" + orderId + ")");
    	Optional<QueryOrder> results = Optional.empty();
    	
    	try {
    		results = orderDAO.getById(orderId);
    		logger.info("OrderQueryService:getOrderById found orderId (" + orderId + ") results: ("+results.isPresent()+")");
    	} catch (Exception e) {
    		e.printStackTrace();
    		logger.error("OrderQueryService:getOrderById encountered an error for orderId ("+orderId+")", e);
    	}
   	
    	return results;
    }
	
	public Collection<QueryOrder> getOrdersByManufacturer(String manuf){
		logger.info("OrderQueryService:getOrdersByManufacturer searching for manufacturer (" + manuf + ")");
		Collection<QueryOrder> results = new ArrayList<QueryOrder>();
		
		try {
			results = orderDAO.getByManuf(manuf);
			logger.info("QueryOrderService:getOrdersByManufacturer found " + results.size() + " orders for manufacturer (" + manuf + ")");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("OrderQueryService:getOrdersByManufacturer encountered an error for manufacturer ("+manuf+")", e);
		}

		return results;
	}
	
	public Collection<QueryOrder> getOrdersByStatus(String status) {
		logger.info("OrderQueryService:getOrdersByStatus searching for status (" + status + ")");
		Collection<QueryOrder> results = new ArrayList<QueryOrder>();
		
		try {
			results = orderDAO.getByStatus(status);
			logger.info("QueryOrderService:getOrdersByStatus found " + results.size() + " orders for status (" + status + ")");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("OrderQueryService:getOrdersByStatus encountered an error for status ("+status+")", e);

		}

		return results;
	}

	public Collection<QueryOrder> getOrdersByContainerId(String containerId) {
		logger.info("OrderQueryService:getOrdersByContainerId searching for order whose container allocated is (" + containerId + ")");
		Collection<QueryOrder> results = new ArrayList<QueryOrder>();
		
		try {
			results = orderDAO.getByContainerId(containerId);
			logger.info("QueryOrderService:getOrdersByContainerId found " + results.size() + " orders for container (" + containerId + ")");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("OrderQueryService:getOrdersByContainerId encountered an error for container ("+containerId+")", e);

		}

		return results;
	}

	public Collection<QueryOrder> getOrders() {
		logger.info("OrderQueryService:getOrders searching for all orders");
		Collection<QueryOrder> results = new ArrayList<QueryOrder>();
		
		try {
			results = orderDAO.getOrders();
			logger.info("QueryOrderService:getOrders found " + results.size() + " orders");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("OrderQueryService:getOrders encountered an error searching for all orders", e);

		}

		return results;
	}

}
