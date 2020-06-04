package ibm.labs.kc.order.query.dao;

import ibm.gse.orderqueryms.domain.model.Address;
import ibm.gse.orderqueryms.domain.model.Order;
import ibm.gse.orderqueryms.domain.model.VoyageAssignment;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistory;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistoryInfo;
import ibm.gse.orderqueryms.infrastructure.events.order.AssignOrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.OrderEvent;
import ibm.gse.orderqueryms.infrastructure.repository.OrderHistoryDAOMock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import static org.junit.Assert.*;

public class OrderActionDAOImplTest {
	
	@Test
    public void testGetOrderHistory() {
		OrderHistoryDAOMock orderActionDAO = new OrderHistoryDAOMock();
        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        
        OrderHistoryInfo orderAction1 = OrderHistoryInfo.newFromOrder(new Order("orderID1", "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));
        
        VoyageAssignment va1 = new VoyageAssignment("orderID1", "myVoyage");
        OrderEvent event1 = new AssignOrderEvent(System.currentTimeMillis(), "1", va1);
        orderAction1.assignVoyage(va1);
        
        OrderHistoryInfo orderAction2 = OrderHistoryInfo.newFromOrder(new Order("orderID2", "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));
        
        VoyageAssignment va2 = new VoyageAssignment("orderID2", "myVoyage");
        OrderEvent event2 = new AssignOrderEvent(System.currentTimeMillis(), "1", va2);
        orderAction2.assignVoyage(va2);
        
        OrderHistory expectedOrderAction1 = OrderHistory.newFromOrder(orderAction1, event1.getTimestampMillis(), event1.getType());
        OrderHistory expectedQueryAction2 = OrderHistory.newFromOrder(orderAction2, event2.getTimestampMillis(), event2.getType());
              
        orderActionDAO.addOrder(expectedOrderAction1);
        orderActionDAO.orderHistory(expectedOrderAction1);
        orderActionDAO.addOrder(expectedQueryAction2);
        orderActionDAO.orderHistory(expectedQueryAction2);

        Collection<OrderHistory> expectedwithID = new ArrayList<>();
        expectedwithID.add(expectedOrderAction1);
        
        Collection<OrderHistory> expected = new ArrayList<>();
        
        for (OrderHistory complexQueryOrder : expectedwithID) {
        	OrderHistory reqOrder = new OrderHistory(complexQueryOrder.getTimestampMillis(), complexQueryOrder.getAction(), complexQueryOrder.getType());
        	expected.add(reqOrder);
        }


        Collection<OrderHistory> byStatus = orderActionDAO.getOrderStatus("orderID1");
        assertArrayEquals(Collections.unmodifiableCollection(expected).toArray(),byStatus.toArray());
    }

}
