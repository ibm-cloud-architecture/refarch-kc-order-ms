package ibm.gse.orderqueryms.app;

import ibm.gse.orderqueryms.infrastructure.repository.OrderHistoryDAO;
import ibm.gse.orderqueryms.infrastructure.repository.OrderHistoryDAOMock;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAO;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAOMock;

public class AppRegistry {

	private static AppRegistry instance = new AppRegistry();
	private static OrderDAO orderRepository;
	private static OrderHistoryDAO orderHistoryRepository;
	
	public static AppRegistry getInstance() {
		return instance;	
	}
   
    public OrderDAO orderRepository() {
    	synchronized(instance) {
	        if (orderRepository == null) {
	        	orderRepository = new OrderDAOMock();
	        }
    	}
        return orderRepository;
    }
    
    public OrderHistoryDAO orderHistoryRepository() {
    	synchronized(instance) {
	        if (orderHistoryRepository == null) {
	        	orderHistoryRepository = new OrderHistoryDAOMock();
	        }
    	}
        return orderHistoryRepository;
    }
	
}
