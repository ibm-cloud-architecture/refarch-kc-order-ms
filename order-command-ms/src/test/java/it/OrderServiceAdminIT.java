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

import ibm.labs.kc.order.command.kafka.ApplicationConfig;
import ibm.labs.kc.order.command.model.Address;
import ibm.labs.kc.order.command.model.Order;
import ibm.labs.kc.order.command.model.events.OrderEvent;

public class OrderServiceAdminIT {

    private String port = System.getProperty("liberty.test.port");
    private String endpoint = "/orders";
    private String url = "http://localhost:" + port + endpoint;

    @Test
    public void testGetAll() throws Exception {
        String orderID = UUID.randomUUID().toString();
        Properties properties = ApplicationConfig.getProducerProperties("testGetById");

        Address addr = new Address("myStreet", "myCity", "myCountry", "myState", "myZipcode");
        Order order = new Order(orderID, "productId", "custId", 2,
                addr, "2019-01-10T13:30Z",
                addr, "2019-01-10T13:30Z");
        OrderEvent event = new OrderEvent(System.currentTimeMillis(), OrderEvent.TYPE_CREATED, "1", order);

        try(Producer<String, String> producer = new KafkaProducer<>(properties)) {
            String value = new Gson().toJson(event);
            String key = order.getOrderID();
            ProducerRecord<String, String> record = new ProducerRecord<>(ApplicationConfig.ORDER_TOPIC, key, value);

            Future<RecordMetadata> future = producer.send(record);
            future.get(10000L, TimeUnit.MILLISECONDS);
        }

        int maxattempts = 10;
        boolean ok = false;
        outer: for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url);
            if (response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                System.out.println(responseString);
                Order[] orders = new Gson().fromJson(responseString, Order[].class);
                for (Order o : orders) {
                    if (orderID.equals(o.getOrderID())) {
                        assertEquals(order, o);
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

    protected Response makeGetRequest(String url) {
        System.out.println("GET " + url);
        Client client = ClientBuilder.newClient();
        Invocation.Builder invoBuild = client.target(url).request();
        Response response = invoBuild.get();
        System.out.println("status " + response.getStatus());
        return response;
    }

}
