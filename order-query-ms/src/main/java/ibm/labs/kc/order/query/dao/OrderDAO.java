package ibm.labs.kc.order.query.dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {

    public Optional<QueryOrder> getById(String orderId);
    public void add(QueryOrder o);
    public void update(QueryOrder order);
    public void orderHistory(QueryOrder o);
    public Collection<QueryOrder> getByManuf(String manuf);
    public Collection<QueryOrder> getByStatus(String status);
    public Collection<QueryOrder> getOrderStatus(String orderID, String customerID);

}
