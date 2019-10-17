package ibm.gse.orderqueryms.domain.service;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderqueryms.domain.model.order.QueryOrder;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAO;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAOMock;

public class OrderQueryService {
    static final Logger logger = LoggerFactory.getLogger(OrderQueryService.class);

    private OrderDAO orderDAO;

    public OrderQueryService() {
        orderDAO = OrderDAOMock.instance();
    }
	
    public Optional<QueryOrder> getOrderById(String orderId){
    	logger.info("OrderQueryService:getOrderById searching for orderId (" + orderId + ")");
    	Optional<QueryOrder> results = orderDAO.getById(orderId);
    	logger.info("OrderQueryService:getOrderById found orderId (" + orderId + ") results: ("+results.isPresent()+")");
    	return results;
    }
	
	public Collection<QueryOrder> getOrdersByManufacturer(String manuf){
		logger.info("OrderQueryService:getOrdersByManufacturer searching for manufacturer (" + manuf + ")");
		Collection<QueryOrder> results = orderDAO.getByManuf(manuf);
		logger.info("QueryOrderService:getOrdersByManufacturer found " + results.size() + " orders for manufacturer (" + manuf + ")");
		return results;
	}
	
	public Collection<QueryOrder> getOrdersByStatus(String status) {
		logger.info("OrderQueryService:getOrdersByStatus searching for status (" + status + ")");
		Collection<QueryOrder> results = orderDAO.getByStatus(status);
		logger.info("QueryOrderService:getOrdersByStatus found " + results.size() + " orders for status (" + status + ")");
		return results;
	}

}
