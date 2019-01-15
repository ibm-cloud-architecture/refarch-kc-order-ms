package ibm.labs.kc.order.command.rest;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
	@Operation(summary = "Request to create an order", description="")
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
		DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
		OffsetDateTime offsetDateTime = OffsetDateTime.parse(co.getExpectedDeliveryDate(), timeFormatter);
		Date expectedDeliveryDate = Date.from(Instant.from(offsetDateTime));
		
		Order order = new Order(""+System.currentTimeMillis(), co.getProductID(), co.getQuantity(), expectedDeliveryDate, co.getStatus());
		
		return Response.ok().entity(order).build();
	}
}