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

import ibm.gse.orderqueryms.domain.model.Address;
import ibm.gse.orderqueryms.domain.model.ContainerAssignment;
import ibm.gse.orderqueryms.domain.model.Order;
import ibm.gse.orderqueryms.domain.model.CancelAndRejectPayload;
import ibm.gse.orderqueryms.domain.model.VoyageAssignment;
import ibm.gse.orderqueryms.domain.model.order.QueryOrder;
import ibm.gse.orderqueryms.infrastructure.events.order.AssignContainerEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.AssignOrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.CreateOrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.OrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.RejectOrderEvent;
import ibm.gse.orderqueryms.infrastructure.kafka.ApplicationConfig;

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
        sendEvent("testGetById", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(event));

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
       sendEvent("testGetByStatus", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(event));

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
       sendEvent("testGetByManuf", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(event));

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
       sendEvent("testHandleVoyageAssignment", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(event));

       VoyageAssignment va = new VoyageAssignment(orderID, "12345");
       OrderEvent event2 = new AssignOrderEvent(System.currentTimeMillis(), "1", va);
       sendEvent("testHandleVoyageAssignment", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(event2));

       QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
       expectedOrder.assignVoyage(va);
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
       sendEvent("testNoAvailability", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(event));

       CancelAndRejectPayload rejectionPayload = new CancelAndRejectPayload(orderID, "productId", "custId", "contId", "voyId", 2, addr, "2019-02-10T13:30Z", addr, "2019-02-10T13:30Z", "rejected", "A container was not found");
       OrderEvent event2 = new RejectOrderEvent(System.currentTimeMillis(), "1", rejectionPayload);
       sendEvent("testNoAvailability", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(event2));

       QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
       expectedOrder.reject(rejectionPayload);
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
       sendEvent("testAllocatedContainer", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(event));

       ContainerAssignment container = new ContainerAssignment(orderID, "myContainer");
       OrderEvent event2 = new AssignContainerEvent(System.currentTimeMillis(), "1", orderID, container);
       sendEvent("testAllocatedContainer", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(event2));

       QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
       expectedOrder.assignContainer(container);
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
    public void testVoyageReady() throws Exception {

    }

    @Test
    public void testOrderSpoiled() throws Exception {

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
