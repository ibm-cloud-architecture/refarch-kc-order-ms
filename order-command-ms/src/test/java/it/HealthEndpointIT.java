package it;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;

import ibm.gse.orderms.app.ShippingOrderResource;

@MicroShedTest
@SharedContainerConfig(ContainerConfig.class)
public class HealthEndpointIT {
  
    @RESTClient
    public static ShippingOrderResource orderResource;
    
    
    @Test
    public void testEndpoint() throws Exception {
        Response rep = orderResource.getOrderByOrderId("01");
        Assertions.assertEquals(404,rep.getStatus());
    }

}
