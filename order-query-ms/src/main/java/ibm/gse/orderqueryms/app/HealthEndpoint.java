package ibm.gse.orderqueryms.app;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@Path("qhealth")
public class HealthEndpoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {
        long now = System.currentTimeMillis();
        return Response.ok("{\"status\":\"UP\", \"when\":\"" + now + "\"}").build();
    }

}
