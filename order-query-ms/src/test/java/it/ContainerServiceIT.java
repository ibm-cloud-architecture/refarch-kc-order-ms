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

import ibm.labs.kc.order.query.dao.QueryContainer;
import ibm.labs.kc.order.query.kafka.ApplicationConfig;
import ibm.labs.kc.order.query.model.Container;
import ibm.labs.kc.order.query.model.events.ContainerEvent;
import ibm.labs.kc.order.query.model.events.AvailableContainerEvent;

public class ContainerServiceIT {
	
	private String port = System.getProperty("liberty.test.port");
    private String endpoint = "/containers/";
    private String url = "http://localhost:" + port + endpoint;

    @Test
    public void testGetByIdContainer() throws Exception {
        String containerID = UUID.randomUUID().toString();

        Container container = new Container(containerID, "brand", "type", 1, 1, 1, "available");
        ContainerEvent event = new AvailableContainerEvent(System.currentTimeMillis(), "1", container);
        sendEvent("testGetByIdContainer", ApplicationConfig.CONTAINER_TOPIC, containerID, new Gson().toJson(event));

        QueryContainer expectedContainer = QueryContainer.newFromContainer(container);
        int maxattempts = 10;
        boolean ok = false;
        for(int i=0; i<maxattempts; i++) {
            Response response = makeGetRequest(url + containerID);
            if(response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                QueryContainer queryContainer = new Gson().fromJson(responseString, QueryContainer.class);
                assertEquals(containerID, queryContainer.getContainerID());
                assertEquals(expectedContainer, queryContainer);
                ok = true;
            } else {
                Thread.sleep(1000L);
            }
        }
        assertTrue(ok);
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
