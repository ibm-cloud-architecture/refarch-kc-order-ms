package it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Test;

import com.google.gson.Gson;

import ibm.labs.kc.order.query.dao.QueryOrder;
import ibm.labs.kc.order.query.kafka.ApplicationConfig;
import ibm.labs.kc.order.query.model.Address;
import ibm.labs.kc.order.query.model.Container;
import ibm.labs.kc.order.query.model.Order;
import ibm.labs.kc.order.query.model.Rejection;
import ibm.labs.kc.order.query.model.VoyageAssignment;
import ibm.labs.kc.order.query.model.events.AllocatedContainerEvent;
import ibm.labs.kc.order.query.model.events.AssignOrderEvent;
import ibm.labs.kc.order.query.model.events.ContainerDeliveredEvent;
import ibm.labs.kc.order.query.model.events.ContainerOffShipEvent;
import ibm.labs.kc.order.query.model.events.ContainerOnShipEvent;
import ibm.labs.kc.order.query.model.events.CreateOrderEvent;
import ibm.labs.kc.order.query.model.events.OrderEvent;
import ibm.labs.kc.order.query.model.events.RejectOrderEvent;

public class QueryServiceIT {
    private String port = System.getProperty("liberty.test.port");
    private String endpoint = "/orders/";
    private String url = "http://localhost:" + port + endpoint;

    @Test
    public void testGetById() throws Exception {
        String orderID = UUID.randomUUID().toString();

        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testGetById", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));

        QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
        int maxattempts = 10;
        boolean ok = false;
        for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + orderID);
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                QueryOrder o = new Gson().fromJson(responseString, QueryOrder.class);
                assertEquals(orderID, o.getOrderID());
                assertEquals(expectedOrder, o);
                ok = true;
            } else {
                Thread.sleep(1000L);
            }
        }
        assertTrue(ok);
    }

    @Test
    public void testGetByStatus() throws Exception {
        String orderID = UUID.randomUUID().toString();

        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testGetByStatus", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));

        int maxattempts = 10;
        boolean ok = false;
        outer: for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + "byStatus/pending");
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                QueryOrder[] orders = new Gson().fromJson(responseString, QueryOrder[].class);
                for (QueryOrder o : orders) {
                    if (orderID.equals(o.getOrderID())) {
                        assertEquals(QueryOrder.newFromOrder(order), o);
                        ok = true;
                        break outer;
                    }
                }
                Thread.sleep(1000L);
            } else {
                Thread.sleep(1000L);
            }
        }
        assertTrue(ok);
    }

    @Test
    public void testGetByManuf() throws Exception {
        String orderID = UUID.randomUUID().toString();

        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testGetByManuf", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));

        QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
        int maxattempts = 10;
        boolean ok = false;
        outer: for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + "byManuf/custId");
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                QueryOrder[] orders = new Gson().fromJson(responseString, QueryOrder[].class);
                for (QueryOrder o : orders) {
                    if (orderID.equals(o.getOrderID())) {
                        assertEquals(expectedOrder, o);
                        ok = true;
                        break outer;
                    }
                }
                Thread.sleep(1000L);
            } else {
                Thread.sleep(1000L);
            }
        }
        assertTrue(ok);
    }

    @Test
    public void testHandleVoyageAssignment() throws Exception {
        String orderID = UUID.randomUUID().toString();

        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testHandleVoyageAssignment", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));

        VoyageAssignment va = new VoyageAssignment(orderID, "12345", "custId", "myShip");
        OrderEvent event2 = new AssignOrderEvent(System.currentTimeMillis(), "1", va);
        sendEvent("testHandleVoyageAssignment", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event2));

        QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
        expectedOrder.assign(va);
        int maxattempts = 10;
        boolean ok = false;
        outer: for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + "byStatus/assigned");
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                QueryOrder[] orders = new Gson().fromJson(responseString, QueryOrder[].class);
                for (QueryOrder o : orders) {
                    if (orderID.equals(o.getOrderID())) {
                        assertEquals(expectedOrder, o);
                        ok = true;
                        break outer;
                    }
                }
                Thread.sleep(1000L);
            } else {
                Thread.sleep(1000L);
            }
        }
        assertTrue(ok);
    }
    
    @Test
    public void testNoAvailability() throws Exception {
        String orderID = UUID.randomUUID().toString();

        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testNoAvailability", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));

        Rejection rejection = new Rejection(orderID, "custId");
        OrderEvent event2 = new RejectOrderEvent(System.currentTimeMillis(), "1", rejection);
        sendEvent("testNoAvailability", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event2));

        QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
        expectedOrder.reject(rejection);
        int maxattempts = 10;
        boolean ok = false;
        outer: for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + "byStatus/rejected");
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                QueryOrder[] orders = new Gson().fromJson(responseString, QueryOrder[].class);
                for (QueryOrder o : orders) {
                    if (orderID.equals(o.getOrderID())) {
                        assertEquals(expectedOrder, o);
                        ok = true;
                        break outer;
                    }
                }
                Thread.sleep(1000L);
            } else {
                Thread.sleep(1000L);
            }
        }
        assertTrue(ok);
    }
    
    @Test
    public void testAllocatedContainer() throws Exception {
    	String orderID = UUID.randomUUID().toString();
    	
    	Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testAllocatedContainer", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));
        
        Container container = new Container(orderID, "myContainer");
        OrderEvent event2 = new AllocatedContainerEvent(System.currentTimeMillis(), "1", container);
        sendEvent("testAllocatedContainer", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event2));
        
        QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
        expectedOrder.allocatedContainer(container);
        int maxattempts = 10;
        boolean ok = false;
        outer: for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + "byStatus/container-allocated");
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                QueryOrder[] orders = new Gson().fromJson(responseString, QueryOrder[].class);
                for (QueryOrder o : orders) {
                    if (orderID.equals(o.getOrderID())) {
                        assertEquals(expectedOrder, o);
                        ok = true;
                        break outer;
                    }
                }
                Thread.sleep(1000L);
            } else {
                Thread.sleep(1000L);
            }
        }
        assertTrue(ok);
    	
    }
    
    @Test
    public void testContainerOnShip() throws Exception {
    	
        String orderID = UUID.randomUUID().toString();
    	
    	Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testContainerOnShip", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));
        
        Container container = new Container(orderID, "myContainer", "myVoyage");
        OrderEvent event2 = new ContainerOnShipEvent(System.currentTimeMillis(), "1", container);
        sendEvent("testContainerOnShip", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event2));
        
        QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
        expectedOrder.containerOnShip(container);
        int maxattempts = 10;
        boolean ok = false;
        outer: for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + "byStatus/container-on-ship");
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                QueryOrder[] orders = new Gson().fromJson(responseString, QueryOrder[].class);
                for (QueryOrder o : orders) {
                    if (orderID.equals(o.getOrderID())) {
                        assertEquals(expectedOrder, o);
                        ok = true;
                        break outer;
                    }
                }
                Thread.sleep(1000L);
            } else {
                Thread.sleep(1000L);
            }
        }
        assertTrue(ok);
    	
    }
    
    @Test
    public void testContainerOffShip() throws Exception {
    	
        String orderID = UUID.randomUUID().toString();
    	
    	Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testContainerOffShip", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));
        
        Container container = new Container(orderID, "myContainer");
        OrderEvent event2 = new ContainerOffShipEvent(System.currentTimeMillis(), "1", container);
        sendEvent("testContainerOffShip", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event2));
        
        QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
        expectedOrder.containerOffShip(container);
        int maxattempts = 10;
        boolean ok = false;
        outer: for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + "byStatus/container-off-ship");
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                QueryOrder[] orders = new Gson().fromJson(responseString, QueryOrder[].class);
                for (QueryOrder o : orders) {
                    if (orderID.equals(o.getOrderID())) {
                        assertEquals(expectedOrder, o);
                        ok = true;
                        break outer;
                    }
                }
                Thread.sleep(1000L);
            } else {
                Thread.sleep(1000L);
            }
        }
        assertTrue(ok);
    	
    }
    
    @Test
    public void testOrderSpoiled() throws Exception {
    	
    }
    
    @Test
    public void testContainerDelivered() throws Exception {
    	
        String orderID = UUID.randomUUID().toString();
    	
    	Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testContainerDelivered", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));
        
        Container container = new Container(orderID, "myContainer");
        OrderEvent event2 = new ContainerDeliveredEvent(System.currentTimeMillis(), "1", container);
        sendEvent("testContainerDelivered", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event2));
        
        QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
        expectedOrder.containerDelivered(container);
        int maxattempts = 10;
        boolean ok = false;
        outer: for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + "byStatus/container-delivered");
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                QueryOrder[] orders = new Gson().fromJson(responseString, QueryOrder[].class);
                for (QueryOrder o : orders) {
                    if (orderID.equals(o.getOrderID())) {
                        assertEquals(expectedOrder, o);
                        ok = true;
                        break outer;
                    }
                }
                Thread.sleep(1000L);
            } else {
                Thread.sleep(1000L);
            }
        }
        assertTrue(ok);
    	
    }
    
    @Test
    public void testOrderCompleted() throws Exception {
    	
    }

    protected Response makeGetRequest(String url) {
        Client client = ClientBuilder.newClient();
        Invocation.Builder invoBuild = client.target(url).request();
        Response response = invoBuild.get();
        return response;
    }

    private void sendEvent(String clientID, String topic, String key, String value) throws Exception {
        Properties properties = ApplicationConfig.getProducerProperties(clientID);

        try(Producer<String, String> producer = new KafkaProducer<>(properties)) {

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            Future<RecordMetadata> future = producer.send(record);
            future.get(10000L, TimeUnit.MILLISECONDS);
        }
    }

}
