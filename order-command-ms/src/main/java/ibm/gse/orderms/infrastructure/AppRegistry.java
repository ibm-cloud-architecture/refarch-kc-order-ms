package ibm.gse.orderms.infrastructure;

import javax.enterprise.context.ApplicationScoped;

import ibm.gse.orderms.app.ShippingOrderResource;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.events.EventEmitterTransactional;
import ibm.gse.orderms.infrastructure.kafka.ErrorEventProducer;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandProducer;
import ibm.gse.orderms.infrastructure.kafka.OrderEventProducer;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;

/**
 * App Registry is a one place to get access to resources of the application.
 * @author jeromeboyer
 *
 */
@ApplicationScoped
public class AppRegistry {

	private ShippingOrderResource orderResource = null;
	
	private static AppRegistry instance = new AppRegistry();

	private static ShippingOrderRepository orderRepository;
	private static OrderCommandProducer orderCommandProducer;
	private static OrderEventProducer orderEventProducer;
	private static ErrorEventProducer errorEventProducer;
	
	public static AppRegistry getInstance() {
		return instance;
		
	}
	
	
	public  ShippingOrderResource orderResource() {
		synchronized(this) {
			if (orderResource == null ) {
				orderResource = new ShippingOrderResource();
			}
		}
		return orderResource;
	}


	
    public ShippingOrderRepository shippingOrderRepository() {
    	synchronized(this) {
	        if (orderRepository == null) {
	        	orderRepository = new ShippingOrderRepositoryMock();
	        }
    	}
        return orderRepository;
    }


	public EventEmitter orderCommandProducer() {
	    	synchronized(this) {
	    		if (orderCommandProducer == null) {
	    			orderCommandProducer = new OrderCommandProducer();
	    		}
	    	}
	        return orderCommandProducer;
	}
	
	public EventEmitterTransactional orderEventProducer() {
    	synchronized(this) {
    		if (orderEventProducer == null) {
    			orderEventProducer = new OrderEventProducer();
    		}
    	}
        return orderEventProducer;
}


	public EventEmitterTransactional errorEventProducer() {
		synchronized(this) {
    		if (errorEventProducer == null) {
    			errorEventProducer = new ErrorEventProducer();
    		}
    	}
        return errorEventProducer;
	}

}
