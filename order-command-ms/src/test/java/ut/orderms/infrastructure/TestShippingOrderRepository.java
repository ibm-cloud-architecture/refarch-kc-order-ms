package ut.orderms.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import ibm.gse.orderms.domain.model.order.Address;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.AppRegistry;
import ibm.gse.orderms.infrastructure.repository.OrderCreationException;
import ibm.gse.orderms.infrastructure.repository.OrderUpdateException;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;


/**
 * Validate the repository behavior
 * 
 * @author jeromeboyer
 *
 */
public class TestShippingOrderRepository {
	
	public static  ShippingOrderRepository repository = AppRegistry.getInstance().shippingOrderRepository();

	/**
	 * At the repository level the ShippingOrder Entity is valid.
	 * As we use the command pattern the caller of the repository API is 
	 * the agent listener for the CreateOrderCommand
	 * @throws OrderCreationException 
	 * @throws OrderUpdateException 
	 */
    @Test
    public void testAddShippingOrderShouldReturnID() {
    	
        Address address = new Address("street", "city", "county", "state", "zipcode");
        
        ShippingOrder order1 = new ShippingOrder(UUID.randomUUID().toString(),
                "productID", "customerID", 1,
                address, "2019-01-10T13:30Z",
                address, "2019-01-10T13:30Z",
                ShippingOrder.PENDING_STATUS);
        

        // verify the repository is empty
        assertEquals(0, repository.getAll().size());

        // Insert one order
        try {
			repository.addOrUpdateNewShippingOrder(order1);
		} catch (OrderCreationException e) {
			e.printStackTrace();
			Assert.fail();
		}
        assertEquals(1, repository.getAll().size());
        assertEquals(order1, repository.getOrderByOrderID(order1.getOrderID()).get());

        // Insert existing key, no new record added
        try {
            repository.addOrUpdateNewShippingOrder(order1);
        } catch (OrderCreationException ise) {
        	System.out.println(ise.getMessage());
        	fail();
        }

        // Update
        order1.setPickupDate("2018-01-10T13:30Z");
        try {
			repository.updateShippingOrder(order1);
		} catch (OrderUpdateException e) {
			e.printStackTrace();
			fail();
		}
       
        assertEquals(1, repository.getAll().size());
        assertEquals(order1, repository.getOrderByOrderID(order1.getOrderID()).get());
        assertEquals("2018-01-10T13:30Z", repository.getOrderByOrderID(order1.getOrderID()).get().getPickupDate());

        // Update non existing key
        ShippingOrder order2 = new ShippingOrder(UUID.randomUUID().toString(),
                "productID", "customerID", 1,
                address, "2019-01-10T13:30Z",
                address, "2019-01-10T13:30Z",
                ShippingOrder.PENDING_STATUS);
        try {
            repository.updateShippingOrder(order2);
            fail();
        } catch (OrderUpdateException ise) {
        	System.out.println(ise.getMessage());
        }

        // Insert
        try {
			repository.addOrUpdateNewShippingOrder(order2);
		} catch (OrderCreationException e) {
			e.printStackTrace();
			fail();
		}
        assertEquals(2, repository.getAll().size());
        ShippingOrder orderOut = repository.getOrderByOrderID(order2.getOrderID()).get();
        assertEquals(order2, orderOut);

        // GetAll
        Collection<ShippingOrder> orders = repository.getAll();
        assertEquals(2, orders.size());
        assertTrue(orders.contains(order1));
        assertTrue(orders.contains(order2));
    }

}
