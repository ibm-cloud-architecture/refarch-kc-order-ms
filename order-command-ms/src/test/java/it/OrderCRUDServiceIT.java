package it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.labs.kc.order.command.dto.OrderCreate;
import ibm.labs.kc.order.command.dto.OrderUpdate;
import ibm.labs.kc.order.command.kafka.ApplicationConfig;
import ibm.labs.kc.order.command.model.Address;
import ibm.labs.kc.order.command.model.Order;
import ibm.labs.kc.order.command.model.events.OrderEvent;

public class OrderCRUDServiceIT {

    private String port = System.getProperty("liberty.test.port");
    private String endpoint = "/orders";
    private String url = "http://localhost:" + port + endpoint;

    @Test
    public void testCreateSuccess() throws Exception {
        System.out.println("Testing endpoint " + url);

        Address address = new Address("street", "city", "county", "state", "zipcode");
        OrderCreate cor = new OrderCreate();
        cor.setProductID("myProductID");
        cor.setCustomerID("GoodManuf");
        cor.setQuantity(100);
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setDestinationAddress(address);
        cor.setPickupAddress(address);

        Response response = makePostRequest(url, new Gson().toJson(cor));
        try {

            int responseCode = response.getStatus();
            assertEquals("Incorrect response code: " + responseCode, 200, responseCode);
            assertTrue(response.hasEntity());
            String responseString = response.readEntity(String.class);

            Order o = new Gson().fromJson(responseString, Order.class);
            assertNotNull(o.getOrderID());
            assertEquals(cor.getProductID(), o.getProductID());
            assertEquals(cor.getQuantity(), o.getQuantity());
            assertEquals(cor.getPickupDate(), o.getPickupDate());
            assertEquals(cor.getExpectedDeliveryDate(), o.getExpectedDeliveryDate());
        } finally {
            response.close();
        }
    }

    @Test
    public void testCreateEmptyJson() throws Exception {
        Response response = makePostRequest(url, "");
        try {
            int responseCode = response.getStatus();
            assertEquals("Incorrect response code: " + responseCode, responseCode, 400);
        } finally {
            response.close();
        }
    }

    @Test
    public void testCreateBadOrderNegativeQuantity() throws Exception {
        OrderCreate cor = new OrderCreate();
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setProductID("myProductID");
        cor.setCustomerID("GoodManuf");

        cor.setQuantity(-100);

        Response response = makePostRequest(url, new Gson().toJson(cor));
        try {
            int responseCode = response.getStatus();
            assertEquals("Incorrect response code: " + responseCode, 400, responseCode);
        } finally {
            response.close();
        }
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        String orderID = UUID.randomUUID().toString();
        String putURL = url + "/" + orderID;
        System.out.println("Testing endpoint: " + putURL);

        Properties properties = ApplicationConfig.getProducerProperties("testUpdateSuccess");

        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z",
                Order.PENDING_STATUS);
        OrderEvent event = new OrderEvent(System.currentTimeMillis(), OrderEvent.TYPE_CREATED, "1", order);

        try(Producer<String, String> producer = new KafkaProducer<>(properties)) {
            String value = new Gson().toJson(event);
            String key = order.getOrderID();
            ProducerRecord<String, String> record = new ProducerRecord<>(ApplicationConfig.ORDER_TOPIC, key, value);

            Future<RecordMetadata> future = producer.send(record);
            future.get(10000, TimeUnit.MILLISECONDS);
        }

        OrderUpdate cor = new OrderUpdate();
        cor.setOrderID(orderID);
        cor.setProductID("myProductID");
        cor.setCustomerID("GoodManuf");
        cor.setQuantity(100);
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        addr.setCity("NYC");
        cor.setDestinationAddress(addr);
        cor.setPickupAddress(addr);

        int maxattempts = 10;
        boolean ok = false;

        for(int i=0; i<maxattempts; i++) {
            Response response = makePutRequest(putURL, new Gson().toJson(cor));
            if (response.getStatus() == 200) {
                assertTrue(response.hasEntity());
                String responseString = response.readEntity(String.class);
                System.out.println(responseString);

                Order o = new Gson().fromJson(responseString, Order.class);
                assertEquals(orderID, o.getOrderID());
                assertEquals(cor.getProductID(), o.getProductID());
                assertEquals(cor.getQuantity(), o.getQuantity());
                assertEquals(cor.getPickupDate(), o.getPickupDate());
                assertEquals(cor.getExpectedDeliveryDate(), o.getExpectedDeliveryDate());
                assertEquals(cor.getDestinationAddress(), o.getDestinationAddress());
                assertEquals(cor.getPickupAddress(), o.getPickupAddress());
                ok = true;
            } else {
                Thread.sleep(1000);
            }
        }
        assertTrue(ok);
    }

    @Test
    public void testUpdateDenied() throws Exception {
        String orderID = UUID.randomUUID().toString();
        String putURL = url + "/" + orderID;
        System.out.println("Testing endpoint: " + putURL);

        Properties properties = ApplicationConfig.getProducerProperties("testUpdateSuccess");

        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z",
                "notPendingStatus");
        OrderEvent event = new OrderEvent(System.currentTimeMillis(), OrderEvent.TYPE_CREATED, "1", order);

        try(Producer<String, String> producer = new KafkaProducer<>(properties)) {
            String value = new Gson().toJson(event);
            String key = order.getOrderID();
            ProducerRecord<String, String> record = new ProducerRecord<>(ApplicationConfig.ORDER_TOPIC, key, value);

            Future<RecordMetadata> future = producer.send(record);
            future.get(10000, TimeUnit.MILLISECONDS);
        }

        OrderUpdate cor = new OrderUpdate();
        cor.setOrderID(orderID);
        cor.setProductID("myProductID");
        cor.setCustomerID("GoodManuf");
        cor.setQuantity(100);
        cor.setPickupDate("2019-01-14T17:48Z");
        cor.setExpectedDeliveryDate("2019-01-15T17:48Z");
        addr.setCity("NYC");
        cor.setDestinationAddress(addr);
        cor.setPickupAddress(addr);

        Response response = makePutRequest(putURL, new Gson().toJson(cor));
        assertEquals(400, response.getStatus());
    }

    protected Response makePutRequest(String url, String json) {
        Client client = ClientBuilder.newClient();
        Invocation.Builder invoBuild = client.target(url).request();
        Response response = invoBuild.put(Entity.json(json));
        return response;
    }

    protected int makeGetRequest(String url) {
        Client client = ClientBuilder.newClient();
        Invocation.Builder invoBuild = client.target(url).request();
        Response response = invoBuild.get();
        int responseCode = response.getStatus();
        response.close();
        return responseCode;
    }

    protected Response makePostRequest(String url, String json) {
        Client client = ClientBuilder.newClient();
        Invocation.Builder invoBuild = client.target(url).request();
        Response response = invoBuild.post(Entity.json(json));
        return response;
    }
}
