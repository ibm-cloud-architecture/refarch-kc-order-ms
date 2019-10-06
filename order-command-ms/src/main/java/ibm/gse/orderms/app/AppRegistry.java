package ibm.gse.orderms.app;

import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandProducer;
import ibm.gse.orderms.infrastructure.kafka.OrderEventProducer;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;

/**
 * App Registry is a one place to get access to resources of the application.
 * @author jeromeboyer
 *
 */
public class AppRegistry {

	private ShippingOrderResource orderResource = null;
	
	private static AppRegistry instance = new AppRegistry();
	private static ShippingOrderRepository orderRepository;
	private static OrderCommandProducer orderCommandProducer;
	private static OrderEventProducer orderEventProducer;
	
	public static AppRegistry getInstance() {
		return instance;
		
	}
	
	
	public  ShippingOrderResource orderResource() {
		synchronized(instance) {
			if (orderResource == null ) {
				orderResource = new ShippingOrderResource();
			}
		}
		return orderResource;
	}


   
    public ShippingOrderRepository shippingOrderRepository() {
    	synchronized(instance) {
	        if (orderRepository == null) {
	        	orderRepository = new ShippingOrderRepositoryMock();
	        }
    	}
        return orderRepository;
    }


	public EventEmitter orderCommandProducer() {
	    	synchronized(instance) {
	    		if (orderCommandProducer == null) {
	    			orderCommandProducer = new OrderCommandProducer();
	    		}
	    	}
	        return orderCommandProducer;
	}
	
	public EventEmitter orderEventProducer() {
    	synchronized(instance) {
    		if (orderEventProducer == null) {
    			orderEventProducer = new OrderEventProducer();
    		}
    	}
        return orderEventProducer;
}

}
