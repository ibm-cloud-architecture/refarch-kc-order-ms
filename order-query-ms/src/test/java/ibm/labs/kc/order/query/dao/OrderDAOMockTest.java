package ibm.labs.kc.order.query.dao;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Test;

import ibm.labs.kc.order.query.model.Address;
import ibm.labs.kc.order.query.model.Order;
import ibm.labs.kc.order.query.model.QueryOrder;

public class OrderDAOMockTest {

    @Test
    public void testGetByManuf() {
        OrderDAOMock dao = new OrderDAOMock();
        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        QueryOrder o1 = QueryOrder.newFromOrder(new Order(UUID.randomUUID().toString(), "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));
        QueryOrder o2 = QueryOrder.newFromOrder(new Order(UUID.randomUUID().toString(), "productId", "custId2", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));
        QueryOrder o3 = QueryOrder.newFromOrder(new Order(UUID.randomUUID().toString(), "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));

        dao.add(o1);
        dao.add(o2);
        dao.add(o3);

        Collection<QueryOrder> expected = new HashSet<>();
        expected.add(o1);
        expected.add(o3);

        Collection<QueryOrder> byManuf1 = dao.getByManuf("custId1");
        assertEquals(expected, new HashSet<QueryOrder>(byManuf1));
    }

}
