package ibm.gse.orderqueryms.infrastructure.repository;

import java.util.Collection;
import java.util.Optional;

import ibm.gse.orderqueryms.domain.model.order.history.OrderHistory;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistoryInfo;

public interface OrderHistoryDAO {
	
	public void addOrder(OrderHistory orderAction);
	public void addContainer(OrderHistory orderAction);
	public void updateOrder(OrderHistory orderAction);
	public void updateContainer(OrderHistory orderAction);
	public Optional<OrderHistoryInfo> getByOrderId(String orderId);
	public Optional<OrderHistoryInfo> getByContainerId(String orderId);
	public void orderHistory(OrderHistory orderAction);
	public void containerHistory(OrderHistory orderAction);
	public Collection<OrderHistory> getOrderStatus(String orderID);

}
