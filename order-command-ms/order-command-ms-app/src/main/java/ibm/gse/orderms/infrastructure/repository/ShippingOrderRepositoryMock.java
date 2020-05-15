package ibm.gse.orderms.infrastructure.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderms.domain.model.order.ShippingOrder;

/**
 * In memory repository... just to make it simple and for testing 
 * We will have a SQL based repo in the future
 *  
 * @author jerome boyer
 *
 */
@ApplicationScoped
public class ShippingOrderRepositoryMock implements ShippingOrderRepository {
    private static final Logger logger = LoggerFactory.getLogger(ShippingOrderRepositoryMock.class);
    private final Map<String, ShippingOrder> orders;
	private boolean failure = false;


    public ShippingOrderRepositoryMock() {
        orders = new ConcurrentHashMap<>();
    }

    @Override
    public void addOrUpdateNewShippingOrder(ShippingOrder order) throws OrderCreationException{
        logger.info("Adding order id " + order.getOrderID());
        // as a mockup we can add unit test controls
        if (this.failure) {
        	throw new OrderCreationException("time out to communicate to database");
        }
        
        if (orders.putIfAbsent(order.getOrderID(), order) != null) {
        	// this has to be re-entrant: for example when replaying the command events 
        	// from events not previously committed due to error. So cancel the duplicate event
        	return ;
        }
    }

    @Override
    public Collection<ShippingOrder> getAll() {
        return Collections.unmodifiableCollection(orders.values());
    }

    @Override
    public void updateShippingOrder(ShippingOrder order) throws OrderUpdateException {
        logger.info("Updating order id " + order.getOrderID());
        // as a mockup we can add unit test controls
        if (this.failure) {
        	throw new OrderUpdateException("time out to communicate to database");
        }
        
        if (orders.replace(order.getOrderID(), order) == null) {
            throw new OrderUpdateException("order does not already exist " + order.getOrderID());
        }
    }

    @Override
    public Optional<ShippingOrder> getOrderByOrderID(String orderId) {
    	logger.info("Get order id " + orderId);
    	ShippingOrder o = orders.get(orderId);
    	if (o != null) logger.info("Get order id retrieve product: " + o.getProductID());
        return Optional.ofNullable(o);
    }

	@Override
	public void reset() {
		orders.values().clear();
		orders.keySet().clear();
	}

	public void injectFailure() {
		this.failure  = true;
	}

	public void resetNormalOperation() {
		this.failure = false;
	}
	
	

}
