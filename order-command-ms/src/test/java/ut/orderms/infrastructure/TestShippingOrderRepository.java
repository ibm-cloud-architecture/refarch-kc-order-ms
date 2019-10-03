package ut.orderms.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.UUID;

import org.junit.Test;

import ibm.gse.orderms.domain.model.order.Address;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;

public class TestShippingOrderRepository {
	
	public static  ShippingOrderRepository repository = ShippingOrderRepositoryMock.instance();

	/**
	 * At the repository level the ShippingOrder Entity is valid.
	 * As we use the command pattern the caller of the repository API is 
	 * the agent listener for the CreateOrderCommand
	 */
    @Test
    public void testAddShippingOrderShouldReturnID() {
    	
        Address address = new Address("street", "city", "county", "state", "zipcode");
        
        ShippingOrder order1 = new ShippingOrder(UUID.randomUUID().toString(),
                "productID", "customerID", 1,
                address, "2019-01-10T13:30Z",
                address, "2019-01-10T13:30Z",
                ShippingOrder.PENDING_STATUS);
        

        // Empty Repository  
        assertEquals(0, repository.getAll().size());

        // Insert
        repository.addNewShippingOrder(order1);
        assertEquals(1, repository.getAll().size());
        assertEquals(order1, repository.getByID(order1.getOrderID()).get());

        // Insert existing key
        try {
            repository.addNewShippingOrder(order1);
            fail();
        } catch (IllegalStateException ise) {
        	System.out.println(ise.getMessage());
        }

        // Update
        order1.setPickupDate("2018-01-10T13:30Z");
        repository.update(order1);
        assertEquals(1, repository.getAll().size());
        assertEquals(order1, repository.getByID(order1.getOrderID()).get());
        assertEquals("2018-01-10T13:30Z", repository.getByID(order1.getOrderID()).get().getPickupDate());

        // Update non existing key
        ShippingOrder order2 = new ShippingOrder(UUID.randomUUID().toString(),
                "productID", "customerID", 1,
                address, "2019-01-10T13:30Z",
                address, "2019-01-10T13:30Z",
                ShippingOrder.PENDING_STATUS);
        try {
            repository.update(order2);
            fail();
        } catch (IllegalStateException ise) {
        	System.out.println(ise.getMessage());
        }

        // Insert
        repository.addNewShippingOrder(order2);
        assertEquals(2, repository.getAll().size());
        ShippingOrder orderOut = repository.getByID(order2.getOrderID()).get();
        assertEquals(order2, orderOut);

        // GetAll
        Collection<ShippingOrder> orders = repository.getAll();
        assertEquals(2, orders.size());
        assertTrue(orders.contains(order1));
        assertTrue(orders.contains(order2));
    }

}
