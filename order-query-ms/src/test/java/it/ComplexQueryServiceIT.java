package it;

import java.util.ArrayList;
import java.util.List;
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
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ibm.labs.kc.order.query.complex.ComplexQueryOrder;
import ibm.labs.kc.order.query.dao.QueryOrder;
import ibm.labs.kc.order.query.kafka.ApplicationConfig;
import ibm.labs.kc.order.query.model.Address;
import ibm.labs.kc.order.query.model.ContainerAssignment;
import ibm.labs.kc.order.query.model.Order;
import ibm.labs.kc.order.query.model.Rejection;
import ibm.labs.kc.order.query.model.VoyageAssignment;
import ibm.labs.kc.order.query.model.events.AssignContainerEvent;
import ibm.labs.kc.order.query.model.events.AssignOrderEvent;
import ibm.labs.kc.order.query.model.events.ContainerDeliveredEvent;
import ibm.labs.kc.order.query.model.events.ContainerOffShipEvent;
import ibm.labs.kc.order.query.model.events.ContainerOnShipEvent;
import ibm.labs.kc.order.query.model.events.CreateOrderEvent;
import ibm.labs.kc.order.query.model.events.OrderCompletedEvent;
import ibm.labs.kc.order.query.model.events.OrderEvent;
import ibm.labs.kc.order.query.model.events.RejectOrderEvent;

public class ComplexQueryServiceIT {
	
	private String port = System.getProperty("liberty.test.port");
    private String endpoint = "/orders/";
    private String url = "http://localhost:" + port + endpoint;
    
    @Test
    public void testOrderStatusNoAvailability() throws Exception {
    	
    	String orderID = UUID.randomUUID().toString();
    	
    	Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
    	Order order = new Order(orderID, "productId", "custId", 2,
    	                addr, "2019-02-10T13:30Z",
    	                addr, "2019-02-10T13:30Z", Order.PENDING_STATUS);
    	OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
    	sendEvent("testNoAvailability", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));
    	
    	Rejection rejection = new Rejection(orderID, "custId");
    	OrderEvent event2 = new RejectOrderEvent(System.currentTimeMillis(), "1", rejection);
    	sendEvent("testNoAvailability", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event2));
    	
    	QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
    	expectedOrder.reject(rejection);
    	ComplexQueryOrder expectedComplexQueryOrder = ComplexQueryOrder.newFromHistoryOrder(expectedOrder, event2.getTimestampMillis(), event2.getType());
        
        Thread.sleep(5000L);
        
        int maxattempts = 10;
     
        for(int i=0; i<maxattempts; i++) {
          Response response = makeGetRequest(url + "orderHistory/"+orderID);
          Assert.assertEquals(response.getStatus(), 200);
          String responseString = response.readEntity(String.class);
          ArrayList<ComplexQueryOrder> complexQueryOrders = new Gson().fromJson(responseString,new TypeToken<List<ComplexQueryOrder>>(){}.getType());
          Assert.assertTrue("orders "+responseString+" expected"+expectedComplexQueryOrder,complexQueryOrders.contains(expectedComplexQueryOrder));
        }

    }
    
    @Test
    public void testOrderStatus() throws Exception {
    	
    	String orderID = UUID.randomUUID().toString();
    	
    	Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testOrderStatus", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event));
        
        QueryOrder expectedOrder = QueryOrder.newFromOrder(order);
        
        VoyageAssignment va = new VoyageAssignment(orderID, "myVoyage");
        OrderEvent event2 = new AssignOrderEvent(System.currentTimeMillis(), "1", va);
        sendEvent("testOrderStatus", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event2));
        
        expectedOrder.assign(va);
        
        ContainerAssignment container = new ContainerAssignment(orderID, "myContainer");
        OrderEvent event3 = new AssignContainerEvent(System.currentTimeMillis(), "1", container);
        sendEvent("testOrderStatus", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event3));
        
        expectedOrder.assignContainer(container);
        
        OrderEvent event4 = new ContainerOnShipEvent(System.currentTimeMillis(), "1", container);
        sendEvent("testOrderStatus", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event4));
        
        expectedOrder.containerOnShip(container);
        
        OrderEvent event5 = new ContainerOffShipEvent(System.currentTimeMillis(), "1", container);
        sendEvent("testOrderStatus", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event5));
        
        expectedOrder.containerOffShip(container);
        
        OrderEvent event6 = new ContainerDeliveredEvent(System.currentTimeMillis(), "1", container);
        sendEvent("testOrderStatus", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event6));
        
        expectedOrder.containerDelivered(container);
        
        Order order1 = new Order(orderID);
        OrderEvent event7 = new OrderCompletedEvent(System.currentTimeMillis(), "1", order1);
        sendEvent("testOrderStatus", ApplicationConfig.ORDER_TOPIC, orderID, new Gson().toJson(event7));
        
        expectedOrder.orderCompleted(order1);
        
    	ComplexQueryOrder expectedComplexQueryOrder = ComplexQueryOrder.newFromHistoryOrder(expectedOrder, event7.getTimestampMillis(), event7.getType());
        
        int maxattempts = 10;
        for(int i=0; i<maxattempts; i++) { 
          Response response = makeGetRequest(url + "orderHistory/"+orderID);
          Assert.assertEquals(response.getStatus(), 200);
          String responseString = response.readEntity(String.class);
          ArrayList<ComplexQueryOrder> complexQueryOrders = new Gson().fromJson(responseString,new TypeToken<List<ComplexQueryOrder>>(){}.getType());
          Assert.assertTrue("orders "+responseString+" expected"+expectedComplexQueryOrder,complexQueryOrders.contains(expectedComplexQueryOrder));
        }
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
