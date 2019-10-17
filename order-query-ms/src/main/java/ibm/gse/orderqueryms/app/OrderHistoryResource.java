package ibm.gse.orderqueryms.app;

import java.util.Collection;

import javax.inject.Inject;
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

import ibm.gse.orderqueryms.domain.model.order.history.OrderHistory;
import ibm.gse.orderqueryms.domain.service.OrderHistoryService;

@Path("orders")
public class OrderHistoryResource {
	static final Logger logger = LoggerFactory.getLogger(OrderHistoryResource.class);
	
	@Inject
	public OrderHistoryService orderHistoryService;

	public OrderHistoryResource() {
		
	}

    public OrderHistoryResource(OrderHistoryService orderHistoryService) {
    	this.orderHistoryService = orderHistoryService;
    }
	
	@GET
    @Path("orderHistory/{orderId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Order history by order ID", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Order history found", content = @Content(mediaType = "application/json")) })
    public Response getOrderHistory(@PathParam("orderId") String orderId) {
        logger.info("OrderHistoryResource.getOrderHistory(" + orderId + ")");
        Collection<OrderHistory> orderAction = this.orderHistoryService.getOrderStatus(orderId);
        return Response.ok(orderAction, MediaType.APPLICATION_JSON).build();
    }

}
