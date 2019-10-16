package ibm.gse.orderqueryms.app;

import ibm.gse.orderqueryms.infrastructure.repository.OrderHistoryDAO;
import ibm.gse.orderqueryms.infrastructure.repository.OrderHistoryDAOMock;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAO;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAOMock;

public class AppRegistry {

	//private ShippingOrderResource orderResource = null;
	
	private static AppRegistry instance = new AppRegistry();
	private static OrderDAO orderRepository;
	private static OrderHistoryDAO orderHistoryRepository;
	//private static OrderCommandProducer orderCommandProducer;
	//private static OrderEventProducer orderEventProducer;
	
	public static AppRegistry getInstance() {
		return instance;
		
	}
	
	/*
	public  ShippingOrderResource orderResource() {
		synchronized(instance) {
			if (orderResource == null ) {
				orderResource = new ShippingOrderResource();
			}
		}
		return orderResource;
	}
	*/
   
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

    /*
	public EventEmitter orderCommandProducer() {
	    	synchronized(instance) {
	    		if (orderCommandProducer == null) {
	    			orderCommandProducer = new OrderCommandProducer();
	    		}
	    	}
	        return orderCommandProducer;
	}
	*/
	
    /*
	public EventEmitter orderEventProducer() {
    	synchronized(instance) {
    		if (orderEventProducer == null) {
    			orderEventProducer = new OrderEventProducer();
    		}
    	}
        return orderEventProducer;
	}
	*/
	
}
