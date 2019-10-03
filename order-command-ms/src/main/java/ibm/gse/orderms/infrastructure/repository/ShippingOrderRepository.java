package ibm.gse.orderms.infrastructure.repository;

import java.util.Collection;
import java.util.Optional;

import ibm.gse.orderms.domain.model.order.ShippingOrder;

public interface ShippingOrderRepository {

    public void addNewShippingOrder(ShippingOrder order);
    public void update(ShippingOrder order);
    public Collection<ShippingOrder> getAll();
    public Optional<ShippingOrder> getByID(String orderId);

}
