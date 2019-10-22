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


    public ShippingOrderRepositoryMock() {
        orders = new ConcurrentHashMap<>();
    }

    @Override
    public void addNewShippingOrder(ShippingOrder order) {
        logger.info("Adding order id " + order.getOrderID());
        if (orders.putIfAbsent(order.getOrderID(), order) != null) {
            throw new IllegalStateException("order already exists " + order.getOrderID());
        }
    }

    @Override
    public Collection<ShippingOrder> getAll() {
        return Collections.unmodifiableCollection(orders.values());
    }

    @Override
    public void updateShippingOrder(ShippingOrder order) {
        logger.info("Updating order id " + order.getOrderID());
        if (orders.replace(order.getOrderID(), order) == null) {
            throw new IllegalStateException("order does not already exist " + order.getOrderID());
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

}
