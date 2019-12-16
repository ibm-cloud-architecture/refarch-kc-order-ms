package ibm.gse.orderqueryms.infrastructure.repository;

import java.util.Collection;
import java.util.Optional;

import ibm.gse.orderqueryms.domain.model.order.QueryOrder;

public interface OrderDAO {

    public Optional<QueryOrder> getById(String orderId);
    public void add(QueryOrder o);
    public void update(QueryOrder order);
    public Collection<QueryOrder> getByManuf(String manuf);
    public Collection<QueryOrder> getByStatus(String status);
    public Collection<QueryOrder> getByContainerId(String containerId);
    public Collection<QueryOrder> getOrders();
    
}
