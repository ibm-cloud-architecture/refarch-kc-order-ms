package ibm.labs.kc.order.query.dao;

import java.util.Collection;
import java.util.Optional;

public interface OrderDAO {

    public Optional<QueryOrder> getById(String orderId);
    public void add(QueryOrder o);
    public void update(QueryOrder order);
    public Collection<QueryOrder> getByManuf(String manuf);
    public Collection<QueryOrder> getByStatus(String status);
    
}
