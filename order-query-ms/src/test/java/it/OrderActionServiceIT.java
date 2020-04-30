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

import ibm.gse.orderqueryms.domain.model.Address;
import ibm.gse.orderqueryms.domain.model.Container;
import ibm.gse.orderqueryms.domain.model.ContainerAssignment;
import ibm.gse.orderqueryms.domain.model.Order;
import ibm.gse.orderqueryms.domain.model.CancelAndRejectPayload;
import ibm.gse.orderqueryms.domain.model.VoyageAssignment;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistory;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistoryInfo;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerAddedEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOffMaintenanceEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOnMaintenanceEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOrderAssignedEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.AssignContainerEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.AssignOrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.CreateOrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.OrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.RejectOrderEvent;
import ibm.gse.orderqueryms.infrastructure.kafka.ApplicationConfig;

public class OrderActionServiceIT {

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
    	OrderEvent ord_event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
    	sendEvent("testOrderStatusNoAvailability", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(ord_event));

        CancelAndRejectPayload rejectionPayload = new CancelAndRejectPayload(orderID, "productId", "custId", "contId", "voyId", 2, addr, "2019-02-10T13:30Z", addr, "2019-02-10T13:30Z", "rejected", "A container was not found");

    	OrderEvent ord_event2 = new RejectOrderEvent(System.currentTimeMillis(), "1", rejectionPayload);
    	sendEvent("testOrderStatusNoAvailability", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(ord_event2));

    	OrderHistoryInfo expectedOrder = OrderHistoryInfo.newFromOrder(order);
    	expectedOrder.reject(rejectionPayload);
    	OrderHistory expectedComplexQueryOrder = OrderHistory.newFromHistoryOrder(expectedOrder, ord_event2.getTimestampMillis(), ord_event2.getType());

        Thread.sleep(10000L);

        int maxattempts = 10;

        for(int i=0; i<maxattempts; i++) {
          Response response = makeGetRequest(url + "orderHistory/"+orderID);
          Assert.assertEquals(response.getStatus(), 200);
          String responseString = response.readEntity(String.class);
          ArrayList<OrderHistory> complexQueryOrders = new Gson().fromJson(responseString,new TypeToken<List<OrderHistory>>(){}.getType());
          Assert.assertTrue(complexQueryOrders.contains(expectedComplexQueryOrder));
        }

    }

    @Test
    public void testOrderStatus() throws Exception {

    	String orderID = UUID.randomUUID().toString();
    	String containerID = UUID.randomUUID().toString();

    	Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z", Order.PENDING_STATUS);
        OrderEvent ord_event = new CreateOrderEvent(System.currentTimeMillis(), "1", order);
        sendEvent("testOrderStatus", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(ord_event));

        OrderHistoryInfo expectedOrder = OrderHistoryInfo.newFromOrder(order);

        VoyageAssignment va = new VoyageAssignment(orderID, "myVoyage");
        OrderEvent ord_event2 = new AssignOrderEvent(System.currentTimeMillis(), "1", va);
        sendEvent("testOrderStatus", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(ord_event2));

        expectedOrder.assignVoyage(va);

        ContainerAssignment container = new ContainerAssignment(orderID, containerID);
        OrderEvent ord_event3 = new AssignContainerEvent(System.currentTimeMillis(), "1", orderID, container);
        sendEvent("testOrderStatus", ApplicationConfig.getOrderTopic(), orderID, new Gson().toJson(ord_event3));

        expectedOrder.assignContainer(container);

        Container cont = new Container(containerID, "brand", "type", 1, 1, 1, "ContainerAdded");
        ContainerEvent cont_event = new ContainerAddedEvent(System.currentTimeMillis(), "1", cont);
        sendEvent("testOrderStatus", ApplicationConfig.getContainerTopic(), containerID, new Gson().toJson(cont_event));

        OrderHistoryInfo expectedContainer = OrderHistoryInfo.newFromContainer(cont);

        // cont.setStatus("ContainerAtLocation");
        // ContainerEvent cont_event2 = new ContainerAtLocationEvent(System.currentTimeMillis(), "1", cont);
        // sendEvent("testOrderStatus", ApplicationConfig.getContainerTopic(), containerID, new Gson().toJson(cont_event2));

        // expectedContainer.containerAtLocation(cont);

        cont.setStatus("ContainerOnMaintenance");
        ContainerEvent cont_event3 = new ContainerOnMaintenanceEvent(System.currentTimeMillis(), "1", cont);
        sendEvent("testOrderStatus", ApplicationConfig.getContainerTopic(), containerID, new Gson().toJson(cont_event3));

        expectedContainer.containerOnMaintenance(cont);
        
        cont.setStatus("ContainerOffMaintenance");
        ContainerEvent cont_event4 = new ContainerOffMaintenanceEvent(System.currentTimeMillis(), "1", cont);
        sendEvent("testOrderStatus", ApplicationConfig.getContainerTopic(), containerID, new Gson().toJson(cont_event4));

        expectedContainer.containerOffMaintenance(cont);
        
        cont.setStatus("ContainerAssignedToOrder");
        ContainerEvent cont_event5 = new ContainerOrderAssignedEvent(System.currentTimeMillis(), "1", cont);
        sendEvent("testOrderStatus", ApplicationConfig.getContainerTopic(), containerID, new Gson().toJson(cont_event5));

        expectedContainer.containerOrderAssignment(cont);

    	OrderHistory expectedComplexQueryOrder = OrderHistory.newFromHistoryOrder(expectedOrder, ord_event3.getTimestampMillis(), ord_event3.getType());
        
    	int maxattempts = 10;

        for(int i=0; i<maxattempts; i++) {
          Response response = makeGetRequest(url + "orderHistory/"+orderID);
          Assert.assertEquals(response.getStatus(), 200);
          String responseString = response.readEntity(String.class);
          ArrayList<OrderHistory> complexQueryOrders = new Gson().fromJson(responseString,new TypeToken<List<OrderHistory>>(){}.getType());
          Assert.assertTrue(complexQueryOrders.contains(expectedComplexQueryOrder));
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
