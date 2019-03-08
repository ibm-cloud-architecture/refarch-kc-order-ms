package ibm.labs.kc.order.query.complex;

import java.util.Collection;

public interface ComplexQueryDAO {
	
	public void add(ComplexQueryOrder complexQueryOrder);
    public void update(ComplexQueryOrder complexQueryOrder);
	public void orderHistory(ComplexQueryOrder complexQueryOrder);
	public Collection<ComplexQueryOrder> getOrderStatus(String orderID);

}
