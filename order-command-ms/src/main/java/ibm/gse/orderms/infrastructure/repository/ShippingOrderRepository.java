package ibm.gse.orderms.infrastructure.repository;

import java.util.Collection;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import ibm.gse.orderms.domain.model.order.ShippingOrder;


public interface ShippingOrderRepository {

    public void addNewShippingOrder(ShippingOrder order);
    public void updateShippingOrder(ShippingOrder order);
    public Collection<ShippingOrder> getAll();
    public Optional<ShippingOrder> getOrderByOrderID(String orderId);
	public void reset();

}
