package ibm.gse.orderms.infrastructure.repository;

import java.util.Collection;
import java.util.Optional;

import ibm.gse.orderms.domain.model.order.ShippingOrder;


public interface ShippingOrderRepository {

    public void addOrUpdateNewShippingOrder(ShippingOrder order) throws OrderCreationException;
    public void updateShippingOrder(ShippingOrder order) throws OrderUpdateException;;
    public Collection<ShippingOrder> getAll();
    public Optional<ShippingOrder> getOrderByOrderID(String orderId);
	public void reset();
	

}
