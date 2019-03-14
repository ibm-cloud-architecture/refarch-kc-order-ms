package ibm.labs.kc.order.query.dao;

import ibm.labs.kc.order.query.action.OrderAction;
import ibm.labs.kc.order.query.action.OrderActionDAOImpl;
import ibm.labs.kc.order.query.action.OrderActionInfo;
import ibm.labs.kc.order.query.model.Address;
import ibm.labs.kc.order.query.model.Order;
import ibm.labs.kc.order.query.model.VoyageAssignment;
import ibm.labs.kc.order.query.model.events.AssignOrderEvent;
import ibm.labs.kc.order.query.model.events.OrderEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import static org.junit.Assert.*;

public class OrderActionDAOImplTest {
	
	@Test
    public void testGetOrderHistory() {
		OrderActionDAOImpl orderActionDAO = new OrderActionDAOImpl();
        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        
        OrderActionInfo orderAction1 = OrderActionInfo.newFromOrder(new Order("orderID1", "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));
        
        VoyageAssignment va1 = new VoyageAssignment("orderID1", "myVoyage");
        OrderEvent event1 = new AssignOrderEvent(System.currentTimeMillis(), "1", va1);
        orderAction1.assign(va1);
        
        OrderActionInfo orderAction2 = OrderActionInfo.newFromOrder(new Order("orderID2", "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));
        
        VoyageAssignment va2 = new VoyageAssignment("orderID2", "myVoyage");
        OrderEvent event2 = new AssignOrderEvent(System.currentTimeMillis(), "1", va2);
        orderAction2.assign(va2);
        
        OrderAction expectedOrderAction1 = OrderAction.newFromOrder(orderAction1, event1.getTimestampMillis(), event1.getType());
        OrderAction expectedQueryAction2 = OrderAction.newFromOrder(orderAction2, event2.getTimestampMillis(), event2.getType());
              
        orderActionDAO.add(expectedOrderAction1);
        orderActionDAO.orderHistory(expectedOrderAction1);
        orderActionDAO.add(expectedQueryAction2);
        orderActionDAO.orderHistory(expectedQueryAction2);

        Collection<OrderAction> expectedwithID = new ArrayList<>();
        expectedwithID.add(expectedOrderAction1);
        
        Collection<OrderAction> expected = new ArrayList<>();
        
        for (OrderAction complexQueryOrder : expectedwithID) {
        	OrderAction reqOrder = new OrderAction(complexQueryOrder.getTimestampMillis(), complexQueryOrder.getAction(), complexQueryOrder.getType());
        	expected.add(reqOrder);
        }


        Collection<OrderAction> byStatus = orderActionDAO.getOrderStatus("orderID1");
        assertArrayEquals(Collections.unmodifiableCollection(expected).toArray(),byStatus.toArray());
    }

}
