package ibm.labs.kc.order.command.dao;

import java.util.Collection;

import ibm.labs.kc.order.command.model.Order;

public interface OrderDAO {

    public void add(Order order);
    public void update(Order order);
    public Collection<Order> getAll();
    public Order getByID(String orderID);

}
