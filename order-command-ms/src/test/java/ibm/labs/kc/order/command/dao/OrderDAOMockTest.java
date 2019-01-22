package ibm.labs.kc.order.command.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.UUID;

import org.junit.Test;

import ibm.labs.kc.order.command.model.Address;
import ibm.labs.kc.order.command.model.Order;

public class OrderDAOMockTest {

    @Test
    public void testAdd() {
        Address address = new Address("street", "city", "county", "state", "zipcode");
        Order order1 = new Order(UUID.randomUUID().toString(), "productID", "customerID", 1, address, "2019-01-10T13:30Z", address, "2019-01-10T13:30Z");

        // Empty DAO
        OrderDAO dao = OrderDAOMock.instance();
        assertEquals(0, dao.getAll().size());
        assertSame(dao, OrderDAOMock.instance());

        // Insert
        dao.add(order1);
        assertEquals(1, dao.getAll().size());
        assertEquals(order1, dao.getByID(order1.getOrderID()));

        // Insert existing key
        try {
            dao.add(order1);
            fail();
        } catch (IllegalStateException ise) {}

        // Update
        order1.setPickupDate("2018-01-10T13:30Z");
        dao.update(order1);
        assertEquals(1, dao.getAll().size());
        assertEquals(order1, dao.getByID(order1.getOrderID()));

        // Update non existing key
        Order order2 = new Order(UUID.randomUUID().toString(), "productID", "customerID", 1, address, "2019-01-10T13:30Z", address, "2019-01-10T13:30Z");
        try {
            dao.update(order2);
            fail();
        } catch (IllegalStateException ise) {}

        // Insert
        dao.add(order2);
        assertEquals(2, dao.getAll().size());
        assertEquals(order2, dao.getByID(order2.getOrderID()));

        // GetAll
        Collection<Order> orders = dao.getAll();
        assertEquals(2, orders.size());
        assertTrue(orders.contains(order1));
        assertTrue(orders.contains(order2));
    }

}
