package ibm.labs.kc.order.query.action;

import java.util.Collection;
import java.util.Optional;

public interface OrderActionDAO {
	
	public void add(OrderAction complexQueryOrder);
	public void update(OrderAction complexQueryOrder);
	public Optional<OrderActionInfo> getById(String orderId);
	public void orderHistory(OrderAction complexQueryOrder);
	public Collection<OrderAction> getOrderStatus(String orderID);

}
