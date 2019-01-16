package ibm.labs.kc.order.query.dao;

import java.util.Optional;

import ibm.labs.kc.order.query.model.Order;

public interface OrderDAO {

    public Optional<Order> getById(String orderId);

}
