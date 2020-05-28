package it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.domain.model.order.Address;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.events.EventBase;

/**
 * This test is to validate SAGA pattern among the order, voyage and container services
 * 
 * Create an order, it is pending
	 Send voyage allocated event, the order is now assigned, its query view has a voyage ID
	 Send container assigned event, the order is now in container allocated, its query view has a container ID too  
 * 
 * @author jerome boyer
 *
 */
public class TestOrderTransaction extends CommonITTest {

  /**
   * 
   * 
   */
	@Test
	public void shouldHaveOrderInPendingUntilContainerAllocated() throws InterruptedException {
		// 1- Create an order	
	    // ###############################
		System.out.println("Testing create order " + url);
        Address addressP = new Address("street", "Oakland", "county", "state", "zipcode");
	    Address addressD = new Address("street", "Shanghai", "county", "state", "zipcode");
		      
	    ShippingOrderCreateParameters cor = new ShippingOrderCreateParameters();
        cor.setProductID("itgTestProduct");
        cor.setCustomerID("TestManuf");
        cor.setQuantity(100);
        cor.setPickupDate("2019-02-14T17:48Z");
        cor.setExpectedDeliveryDate("2019-03-15T17:48Z");
        cor.setDestinationAddress(addressD);
        cor.setPickupAddress(addressP);
	    Response response = makePostRequest(url, new Gson().toJson(cor));
	    int responseCode = response.getStatus();
        assertEquals("Incorrect response code: " + responseCode, 200, responseCode);
        assertTrue(response.hasEntity());
    	String orderID = response.readEntity(String.class);
    	assertNotNull(orderID);
    	response.close();
    	
    	System.out.println(" Wait the command event is created and handled by agent");
    	Thread.sleep(5000);
    	 
    	response = makeGetRequest(url + "/" + orderID);
        String responseString = response.readEntity(String.class);
        ShippingOrder persistedOrder  = new Gson().fromJson(responseString, ShippingOrder.class);
        Assert.assertTrue(ShippingOrder.PENDING_STATUS.contentEquals(persistedOrder.getStatus()));

        response.close();
        
	    System.out.println("orderID = " + orderID);
	    
	    // 2- Allocate a voyage	
	    // ###############################
	    Properties properties = new Properties();
	    properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, System.getenv().get("KAFKA_BROKERS"));
	    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.CLIENT_ID_CONFIG, "testIgClient");
		properties.put(ProducerConfig.ACKS_CONFIG, "1");
		properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
		properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		properties.put(SaslConfigs.SASL_JAAS_CONFIG,
				"org.apache.kafka.common.security.plain.PlainLoginModule required username=\"token\" password=\""
						+ System.getenv().get("KAFKA_APIKEY") + "\";");
		properties.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.2");
		properties.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2");
		properties.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");
		
	    //KafkaInfrastructureConfig.getProducerProperties("test-event-producer");
	    KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);
	    String voyageEvent = "{\"timestamp\": " + new Date().getTime() 
	    		+ ",\"type\": \"" + EventBase.TYPE_VOYAGE_ASSIGNED + "\", \"version\": \""
	    		+ "1" + "\"," 
	    		+ " \"payload\": { \"voyageID\": \"V101\",\"orderID\": \"" + orderID
	    		+ "\"}}";
	    ProducerRecord<String, String> record = new ProducerRecord<>("orders", orderID, voyageEvent);
	    System.out.println("Mockup voyage service with " + voyageEvent);
        Future<RecordMetadata> send = kafkaProducer.send(record);
        try {
			send.get(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Interruption" + e.getMessage());
		} catch (ExecutionException e) {
			e.printStackTrace();
			fail("Interruption" + e.getMessage());
		} catch (TimeoutException e) {
			e.printStackTrace();
			fail("Interruption" + e.getMessage());
		} finally {
			kafkaProducer.close();
		}
        Thread.sleep(10000);
        
        // 3 verify the status is still pending as there is only a voyage
        // ###############################
        Response res = makeGetRequest(url + "/" + orderID);
        ShippingOrder o = new Gson().fromJson(res.readEntity(String.class), ShippingOrder.class);
        assertEquals(ShippingOrder.PENDING_STATUS,o.getStatus());
       
        // the voyage ID is set on the query side of the order service.
        // 4- now allocate the container: the container service is listening to event that the voyage id is assigned to container id so it search for a container and assiend it.
        // ###############################
        kafkaProducer = new KafkaProducer<String, String>(properties);
	    String containerEvent = "{\"timestamp\": " + new Date().getTime() 
	    		+ ",\"type\": \"" + EventBase.TYPE_CONTAINER_ALLOCATED + "\", \"version\": \"1\"," 
	    		+ " \"payload\": { \"containerID\": \"c02\",\"orderID\": \"" + orderID
	    		+ "\"}}";
	    record = new ProducerRecord<>("orders", orderID, containerEvent);
	    System.out.println("Mockup container service with " + containerEvent);
        send = kafkaProducer.send(record);
        try {
			send.get(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Interuption" + e.getMessage());
		} finally {
			kafkaProducer.close();
		}
        Thread.sleep(10000);
        // 5 verify the status is now assigned as there is a container assigned
        // ###############################
        res = makeGetRequest(url + "/" + orderID);
        o = new Gson().fromJson(res.readEntity(String.class), ShippingOrder.class);
        assertEquals(ShippingOrder.ASSIGNED_STATUS,o.getStatus());
	}

}
