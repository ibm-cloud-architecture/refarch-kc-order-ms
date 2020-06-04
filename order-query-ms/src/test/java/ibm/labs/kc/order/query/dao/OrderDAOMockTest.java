package ibm.labs.kc.order.query.dao;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

import ibm.gse.orderqueryms.domain.model.Address;
import ibm.gse.orderqueryms.domain.model.Order;
import ibm.gse.orderqueryms.domain.model.order.QueryOrder;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAOMock;

public class OrderDAOMockTest {
	
	@Test
    public void testGetById() {
		OrderDAOMock dao = new OrderDAOMock();
        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        QueryOrder o1 = QueryOrder.newFromOrder(new Order("orderID", "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));
        QueryOrder o2 = QueryOrder.newFromOrder(new Order(UUID.randomUUID().toString(), "productId", "custId2", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));

        dao.add(o1);
        dao.add(o2);

        Optional<QueryOrder> byId = dao.getById("orderID");
        assertEquals(o1, byId.orElse(null));
    }

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
    
    @Test
    public void testGetByStatus() {
        OrderDAOMock dao = new OrderDAOMock();
        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        QueryOrder o1 = QueryOrder.newFromOrder(new Order(UUID.randomUUID().toString(), "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS));
        QueryOrder o2 = QueryOrder.newFromOrder(new Order(UUID.randomUUID().toString(), "productId", "custId2", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.ASSIGNED_STATUS));
        QueryOrder o3 = QueryOrder.newFromOrder(new Order(UUID.randomUUID().toString(), "productId", "custId1", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.ASSIGNED_STATUS));

        dao.add(o1);
        dao.add(o2);
        dao.add(o3);

        Collection<QueryOrder> expected = new HashSet<>();
        expected.add(o2);
        expected.add(o3);

        Collection<QueryOrder> byStatus = dao.getByStatus(Order.ASSIGNED_STATUS);
        assertEquals(expected, new HashSet<QueryOrder>(byStatus));
    }

}
