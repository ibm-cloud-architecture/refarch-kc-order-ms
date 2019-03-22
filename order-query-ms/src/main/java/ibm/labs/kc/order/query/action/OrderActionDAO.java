package ibm.labs.kc.order.query.action;

import java.util.Collection;
import java.util.Optional;

public interface OrderActionDAO {
	
	public void addOrder(OrderAction orderAction);
	public void addContainer(OrderAction orderAction);
	public void updateOrder(OrderAction orderAction);
	public void updateContainer(OrderAction orderAction);
	public Optional<OrderActionInfo> getByOrderId(String orderId);
	public Optional<OrderActionInfo> getByContainerId(String orderId);
	public void orderHistory(OrderAction orderAction);
	public void containerHistory(OrderAction orderAction);
	public Collection<OrderAction> getOrderStatus(String orderID);

}
