package ibm.labs.kc.order.command.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
        Order order1 = new Order(UUID.randomUUID().toString(),
                "productID", "customerID", 1,
                address, "2019-01-10T13:30Z",
                address, "2019-01-10T13:30Z",
                Order.PENDING_STATUS);
        CommandOrder co1 = CommandOrder.newFromOrder(order1);
        CommandOrder co1Clone = CommandOrder.newFromOrder(order1);

        // Empty DAO
        OrderDAO dao = OrderDAOMock.instance();
        assertEquals(0, dao.getAll().size());
        assertSame(dao, OrderDAOMock.instance());

        // Insert
        dao.add(co1);
        assertEquals(1, dao.getAll().size());
        assertEquals(co1Clone, dao.getByID(order1.getOrderID()).get());

        // Insert existing key
        try {
            dao.add(co1);
            fail();
        } catch (IllegalStateException ise) {}

        // Update
        order1.setPickupDate("2018-01-10T13:30Z");
        co1Clone = CommandOrder.newFromOrder(order1);
        dao.update(co1Clone);
        assertEquals(1, dao.getAll().size());
        assertEquals(co1Clone, dao.getByID(order1.getOrderID()).get());
        assertNotEquals(order1, dao.getByID(order1.getOrderID()).get());

        // Update non existing key
        Order order2 = new Order(UUID.randomUUID().toString(),
                "productID", "customerID", 1,
                address, "2019-01-10T13:30Z",
                address, "2019-01-10T13:30Z",
                Order.PENDING_STATUS);
        CommandOrder co2 = CommandOrder.newFromOrder(order2);
        try {
            dao.update(co2);
            fail();
        } catch (IllegalStateException ise) {}

        // Insert
        dao.add(co2);
        assertEquals(2, dao.getAll().size());
        CommandOrder co2Clone = CommandOrder.newFromOrder(order2);
        assertEquals(co2Clone, dao.getByID(order2.getOrderID()).get());

        // GetAll
        Collection<CommandOrder> orders = dao.getAll();
        assertEquals(2, orders.size());
        assertTrue(orders.contains(co1Clone));
        assertTrue(orders.contains(co2Clone));
    }

}
