package ibm.labs.kc.order.command.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import ibm.labs.kc.order.command.dto.CreateOrderRequest;
import ibm.labs.kc.order.command.model.Order;


@Path("orders")
public class Endpoint {

	/**
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Start a simulation on the given ship name", description="Start a ship simulation by moving the ship and send container metrics events according to the scenarion selected")
    @APIResponses(
            value = {
            	@APIResponse(
                    responseCode = "400", 
                    description = "Bad create order request",
                    content = @Content(mediaType = "text/plain")),
                @APIResponse(
                    responseCode = "200",
                    description = "Order created",
                    content = @Content(mediaType = "application/json"))
            	})
	public Response create(CreateOrderRequest co) {
		Order order = new Order();	
		order.setOrderID(""+System.currentTimeMillis());
		
		order.setProductID(co.getProductID());
		
		return Response.ok().entity(order).build();
	}
}
