package it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import com.google.gson.Gson;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;

import ibm.gse.orderms.app.ShippingOrderResource;
import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.app.dto.ShippingOrderUpdateParameters;
import ibm.gse.orderms.domain.events.order.OrderEvent;
import ibm.gse.orderms.domain.events.order.OrderEventPayload;
import ibm.gse.orderms.domain.model.order.Address;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.kafka.KafkaInfrastructureConfig;
import ut.ShippingOrderTestDataFactory;

/**
 * This is an integration test to create shipping order use HTTP API and kafka
 * backbone
 * 
 * @author jerome boyer
 *
 */

@MicroShedTest
@SharedContainerConfig(ContainerConfig.class)
public class OrderCRUServiceIT extends CommonIntegrationTest {

    @RESTClient
    public static ShippingOrderResource orderResource;

    @Test
    @Order(1)
    public void should_get_orderID_after_posting_order() throws Exception {
        System.out.println("Testing create order " + url);
        ShippingOrderCreateParameters orderDTO = ShippingOrderTestDataFactory.orderCreateFixtureWithoutID();
        Response response = orderResource.createShippingOrder(orderDTO);
        assertEquals(200, response.getStatus());
        assertTrue(response.hasEntity());
        String responseString = response.readEntity(String.class);
        assertNotNull(responseString);
        System.out.println(" --> created order ID: " + responseString);
        response.close();
    }

    @Test
    @Order(2)
    public void should_generate_500_for_empty_order_posted() throws Exception {
        Response response = orderResource.createShippingOrder(null);
        assertEquals(500, response.getStatus());
        response.close();
    }

    @Test
    @Order(3)
    public void should_generate_error_when_order_has_negative_quantity() throws Exception {
        ShippingOrderCreateParameters orderDTO = ShippingOrderTestDataFactory.orderCreateFixtureWithoutID();
        orderDTO.setQuantity(-100);

        Response response = orderResource.createShippingOrder(orderDTO); 
        int responseCode = response.getStatus();
        assertEquals(400, responseCode);
        response.close();    
    }

    @Test
    public void shouldUpdateTheQuantity() throws Exception {
        ShippingOrderCreateParameters orderDTO = ShippingOrderTestDataFactory.orderCreateFixtureWithoutID();
        Response response = makePostRequest(url, new Gson().toJson(orderDTO));
        String orderJson = response.readEntity(String.class);
        OrderEventPayload order = new Gson().fromJson(orderJson, OrderEventPayload.class);
        String orderID = order.getOrderID();
        System.out.println("shouldUpdateTheQuantity for " + orderID);
        // Wait the command event to be produced and consumed and data persisted
        Thread.sleep(5000);
        // now modify the quantity
        ShippingOrderUpdateParameters cor = new ShippingOrderUpdateParameters();
        cor.setOrderID(orderID);
        cor.setProductID(orderDTO.getProductID());
        cor.setCustomerID(orderDTO.getCustomerID());
        cor.setExpectedDeliveryDate(orderDTO.getExpectedDeliveryDate());
        cor.setDestinationAddress(orderDTO.getDestinationAddress());
        cor.setPickupDate(orderDTO.getPickupDate());
        cor.setPickupAddress(orderDTO.getPickupAddress());
        cor.setQuantity(555);
        response = makePutRequest(url + "/" + orderID, new Gson().toJson(cor));
        int responseCode = response.getStatus();

        assertEquals(200, responseCode);

        response = makeGetRequest(url + "/" + orderID);
        String responseString = response.readEntity(String.class);
        ShippingOrder persistedOrder = new Gson().fromJson(responseString, ShippingOrder.class);
        Assertions.assertTrue(555 == persistedOrder.getQuantity());
        response.close();
    }

    @Test
    public void shouldGetSomeRecords() throws InterruptedException {
        ShippingOrderCreateParameters orderDTO = ShippingOrderTestDataFactory.orderCreateFixtureWithoutID();
        Response response = makePostRequest(url, new Gson().toJson(orderDTO));
        int maxattempts = 5;
        outer: for (int i = 0; i < maxattempts; i++) {
            response = makeGetRequest(url);
            if (response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                System.out.println(responseString);
                ShippingOrder[] orders = new Gson().fromJson(responseString, ShippingOrder[].class);
                Assertions.assertNotNull(orders);
                Assertions.assertTrue(orders.length > 0);
                break outer;

            } else {
                Thread.sleep(1000L);
            }
        }
        response.close();
    }

    public void testUpdateDenied() throws Exception {
        String orderID = UUID.randomUUID().toString();
        String putURL = url + "/" + orderID;
        System.out.println("Testing endpoint: " + putURL);

        Properties properties = KafkaInfrastructureConfig.getProducerProperties("testUpdateSuccess");

        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        ShippingOrder order = new ShippingOrder(orderID, "productId", "custId", 2, addr, "2019-01-10T13:30Z", addr,
                "2019-01-10T13:30Z", "notPendingStatus");
        OrderEvent event = new OrderEvent(System.currentTimeMillis(), OrderEvent.ORDER_CREATED_TYPE, "1",
                order.toShippingOrderPayload());

        try (Producer<String, String> producer = new KafkaProducer<>(properties)) {
            String value = new Gson().toJson(event);
            String key = order.getOrderID();
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    new KafkaInfrastructureConfig().getOrderTopic(), key, value);

            Future<RecordMetadata> future = producer.send(record);
            future.get(10000, TimeUnit.MILLISECONDS);
        }

        ShippingOrderUpdateParameters cor = new ShippingOrderUpdateParameters();
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

}
