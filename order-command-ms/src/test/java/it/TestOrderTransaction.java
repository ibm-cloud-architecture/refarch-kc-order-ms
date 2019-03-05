package it;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.labs.kc.order.command.dto.OrderCreate;
import ibm.labs.kc.order.command.kafka.ApplicationConfig;
import ibm.labs.kc.order.command.model.Address;
import ibm.labs.kc.order.command.model.Order;

/**
 * This test is to validate SAGA pattern among the order, voyage and container services
 * 
 * Create an order, it is pending
	 Send voyage allocated event, the order is now assigned, it query view has a voyage ID
	 Send container assigned event, the order is now in container allocated, its query view has a container ID too  
 * 
 * @author jerome boyer
 *
 */
public class TestOrderTransaction extends CommonITTest {

  /**
 * @throws InterruptedException 
   * 
   */
	@Test
	public void testOrderInPendingUntilContainerAllocated() throws InterruptedException {
		System.out.println("Testing create order " + url);
        Address addressP = new Address("street", "Oakland", "county", "state", "zipcode");
	    Address addressD = new Address("street", "Shanghai", "county", "state", "zipcode");
		      
	    OrderCreate cor = new OrderCreate();
        cor.setProductID("itgTestProduct");
        cor.setCustomerID("TestManuf");
        cor.setQuantity(100);
        cor.setPickupDate("2019-02-14T17:48Z");
        cor.setExpectedDeliveryDate("2019-03-15T17:48Z");
        cor.setDestinationAddress(addressD);
        cor.setPickupAddress(addressP);
        String orderID ="";
	    Response response = makePostRequest(url, new Gson().toJson(cor));
	    try {
	        int responseCode = response.getStatus();
	        assertEquals("Incorrect response code: " + responseCode, 200, responseCode);
	        assertTrue(response.hasEntity());
	        String responseString = response.readEntity(String.class);

            Order o = new Gson().fromJson(responseString, Order.class);
            assertNotNull(o.getOrderID());
            orderID=o.getOrderID();
            assertEquals(o.getStatus(), "pending");
        
        } finally {
            response.close();
        }
	    System.out.println("orderID = " + orderID);
	    // 2- Allocate a voyage	
	    Properties properties = ApplicationConfig.getProducerProperties("test-event-producer");
	    KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);
	    String voyageEvent = "{\"timestamp\": " + new Date().getTime() 
	    		+ ",\"type\": \"OrderAssigned\", \"version\": \"1\"," 
	    		+ " \"payload\": { \"voyageID\": \"v101\",\"orderID\": \"" + orderID
	    		+ "\"}}";
	    ProducerRecord<String, String> record = new ProducerRecord<>(ApplicationConfig.ORDER_TOPIC, orderID, voyageEvent);
	    System.out.println("Mockup voyage service with " + voyageEvent);
        Future<RecordMetadata> send = kafkaProducer.send(record);
        try {
			send.get(ApplicationConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Interuption" + e.getMessage());
		} catch (ExecutionException e) {
			e.printStackTrace();
			fail("Interuption" + e.getMessage());
		} catch (TimeoutException e) {
			e.printStackTrace();
			fail("Interuption" + e.getMessage());
		} finally {
			kafkaProducer.close();
		}
        Thread.sleep(10000);
        // 3 verify the status is now assigned as there is a voyage
        Response res = makeGetRequest(url + "/" + orderID);
        Order o = new Gson().fromJson(res.readEntity(String.class), Order.class);
        assertEquals("assigned",o.getStatus());
        // the voyage ID is set on the query side of the order service.
        // 4- now allocate the container: the container service is listening to event that the voyage id is assigned to container id so it search for a container and assiend it.
        kafkaProducer = new KafkaProducer<String, String>(properties);
	    String containerEvent = "{\"timestamp\": " + new Date().getTime() 
	    		+ ",\"type\": \"ContainerAllocated\", \"version\": \"1\"," 
	    		+ " \"payload\": { \"containerID\": \"c02\",\"orderID\": \"" + orderID
	    		+ "\"}}";
	    record = new ProducerRecord<>(ApplicationConfig.ORDER_TOPIC, orderID, containerEvent);
	    System.out.println("Mockup container service with " + containerEvent);
        send = kafkaProducer.send(record);
        try {
			send.get(ApplicationConfig.PRODUCER_TIMEOUT_SECS, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Interuption" + e.getMessage());
		} finally {
			kafkaProducer.close();
		}
        Thread.sleep(10000);
        // 5 verify the status is now assigned as there is a voyage
        res = makeGetRequest(url + "/" + orderID);
        o = new Gson().fromJson(res.readEntity(String.class), Order.class);
        assertEquals("container-allocated",o.getStatus());
	}

}
