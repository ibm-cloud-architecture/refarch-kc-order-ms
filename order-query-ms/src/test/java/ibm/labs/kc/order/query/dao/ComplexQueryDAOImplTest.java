package ibm.labs.kc.order.query.dao;

import ibm.labs.kc.order.query.complex.ComplexQueryDAOImpl;
import ibm.labs.kc.order.query.complex.ComplexQueryOrder;
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

public class ComplexQueryDAOImplTest {
	
	@Test
    public void testGetOrderHistory() {
		ComplexQueryDAOImpl complexQueryDAO = new ComplexQueryDAOImpl();
        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        
        QueryOrder queryOrder1 = QueryOrder.newFromOrder(new Order("orderID1", "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));
        
        VoyageAssignment va1 = new VoyageAssignment("orderID1", "myVoyage");
        OrderEvent event1 = new AssignOrderEvent(System.currentTimeMillis(), "1", va1);
        queryOrder1.assign(va1);
        
        QueryOrder queryOrder2 = QueryOrder.newFromOrder(new Order("orderID2", "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));
        
        VoyageAssignment va2 = new VoyageAssignment("orderID2", "myVoyage");
        OrderEvent event2 = new AssignOrderEvent(System.currentTimeMillis(), "1", va2);
        queryOrder2.assign(va2);
        
        ComplexQueryOrder expectedComplexQueryOrder1 = ComplexQueryOrder.newFromOrder(queryOrder1, event1.getTimestampMillis(), event1.getType());
        ComplexQueryOrder expectedComplexQueryOrder2 = ComplexQueryOrder.newFromOrder(queryOrder2, event2.getTimestampMillis(), event2.getType());
              
        complexQueryDAO.add(expectedComplexQueryOrder1);
        complexQueryDAO.orderHistory(expectedComplexQueryOrder1);
        complexQueryDAO.add(expectedComplexQueryOrder2);
        complexQueryDAO.orderHistory(expectedComplexQueryOrder2);

        Collection<ComplexQueryOrder> expectedwithID = new ArrayList<>();
        expectedwithID.add(expectedComplexQueryOrder1);
        
        Collection<ComplexQueryOrder> expected = new ArrayList<>();
        
        for (ComplexQueryOrder complexQueryOrder : expectedwithID) {
        	ComplexQueryOrder reqOrder = new ComplexQueryOrder(complexQueryOrder.getTimestampMillis(), complexQueryOrder.getAction(), complexQueryOrder.getType());
        	expected.add(reqOrder);
        }


        Collection<ComplexQueryOrder> byStatus = complexQueryDAO.getOrderStatus("orderID1");
        assertArrayEquals(Collections.unmodifiableCollection(expected).toArray(),byStatus.toArray());
    }

}
