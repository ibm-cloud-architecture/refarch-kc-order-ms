package ibm.labs.kc.order.query.complex;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("orders")
public class ComplexQueryService{
	
	static final Logger logger = LoggerFactory.getLogger(ComplexQueryService.class);

    private ComplexQueryDAO complexQueryDAO;

    public ComplexQueryService() {
    	complexQueryDAO = ComplexQueryDAOImpl.instance();
    }
	
	@GET
    @Path("orderHistory/{orderId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Query order history by order ID for a particular customer", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Order history found", content = @Content(mediaType = "application/json")) })
    public Response getOrderHistory(@PathParam("orderId") String orderId) {
        logger.info("QueryService.getOrderHistory(" + orderId + ")");
        Collection<ComplexQueryOrder> complexQueryorder = complexQueryDAO.getOrderStatus(orderId);
        return Response.ok(complexQueryorder, MediaType.APPLICATION_JSON).build();
    }

}
