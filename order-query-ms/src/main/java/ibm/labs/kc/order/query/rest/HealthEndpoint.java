package ibm.labs.kc.order.query.rest;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("qhealth")
public class HealthEndpoint {
    static final Logger logger = Logger.getLogger(HealthEndpoint.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {
      /*
      if (!healthy) {
        return Response.status(503).entity("{\"status\":\"DOWN\"}").build();
      }
      */
        long now = System.currentTimeMillis();
        System.out.println("healthcheck " + now);
        return Response.ok("{\"status\":\"UP\", \"when\":\"" + now + "\"}").build();
    }

}
