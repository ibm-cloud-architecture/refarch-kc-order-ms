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

import ibm.labs.kc.order.query.kafka.ApplicationConfig;
import ibm.labs.kc.order.query.model.Order;

public class QueryServiceIT {
    private String port = System.getProperty("liberty.test.port");
    private String endpoint = "/orders/";
    private String url = "http://localhost:" + port + endpoint;

    @Test
    public void testGetById() throws Exception {
        String orderId = UUID.randomUUID().toString();
                
        Properties properties = ApplicationConfig.getProducerProperties("testGetById");
        try(Producer<String, String> producer = new KafkaProducer<>(properties)) {

            Order o = new Order(orderId,"product123",1,"2019-01-16T17:30T","testStatus");
            String orderString = new Gson().toJson(o);
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(ApplicationConfig.ORDER_TOPIC, o.getOrderID(), orderString); 

            Future<RecordMetadata> future = producer.send(record);
            future.get(10000, TimeUnit.MILLISECONDS);
        }

        int maxattempts = 10;
        boolean ok = false;
        for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + orderId);
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                Order o2 = new Gson().fromJson(responseString, Order.class);
                assertEquals(orderId, o2.getOrderID());
                ok = true;
                break;
            } else {
                Thread.sleep(1000);
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
