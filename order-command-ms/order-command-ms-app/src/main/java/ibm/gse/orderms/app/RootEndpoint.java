package ibm.gse.orderms.app;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class RootEndpoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listResources(@Context UriInfo uriInfo) {
        String healthURL = (uriInfo.getAbsolutePath() + "/health").replaceAll("(?<!http:)\\/\\/", "/");
        return Response.ok("{\"kc order command ms health\":\"" + healthURL + "\"}").build();
    }

    @GET
    @Produces({ MediaType.TEXT_HTML })
    public InputStream getIndex() {
        try {
            return this.getClass().getResourceAsStream("/index.html");
        } catch (Exception e) {
            throw new RuntimeException("Exception returning index.html", e);
        }
    }
}
