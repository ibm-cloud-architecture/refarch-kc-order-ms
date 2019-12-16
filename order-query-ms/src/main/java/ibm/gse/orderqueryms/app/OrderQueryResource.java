package ibm.gse.orderqueryms.app;

import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderqueryms.domain.model.order.QueryOrder;
import ibm.gse.orderqueryms.domain.service.OrderQueryService;

@Path("orders")
public class OrderQueryResource {
    static final Logger logger = LoggerFactory.getLogger(OrderQueryResource.class);

	@Inject
	public OrderQueryService orderQueryService;

    public OrderQueryResource() {

    }
    
    public OrderQueryResource(OrderQueryService orderQueryService) {
    	this.orderQueryService = orderQueryService;
    }

    @GET
    @Path("{Id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Query an order by id", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "text/plain")),
            @APIResponse(responseCode = "200", description = "Order found", content = @Content(mediaType = "application/json")) })
    public Response getById(@PathParam("Id") String orderId) {
        logger.info("OrderQueryResource.getById(" + orderId + ")");

        Optional<QueryOrder> oo = this.orderQueryService.getOrderById(orderId);
        if (oo.isPresent()) {
            return Response.ok().entity(oo.get()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("byManuf/{manuf}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Query orders by manuf", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Orders found", content = @Content(mediaType = "application/json")) })
    public Response getByManuf(@PathParam("manuf") String manuf) {
        logger.info("OrderQueryResource.getByManuf(" + manuf + ")");

        Collection<QueryOrder> orders = this.orderQueryService.getOrdersByManufacturer(manuf);
        return Response.ok().entity(orders).build();
    }

    @GET
    @Path("byStatus/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Query orders by status", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Orders found", content = @Content(mediaType = "application/json")) })
    public Response getByStatus(@PathParam("status") String status) {
        logger.info("OrderQueryResource.getByStatus(" + status + ")");

        Collection<QueryOrder> orders = this.orderQueryService.getOrdersByStatus(status);
        return Response.ok().entity(orders).build();
    }

    @GET
    @Path("byContainerId/{containerId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Query orders by containerId", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Orders found", content = @Content(mediaType = "application/json")) })
    public Response getByContainerId(@PathParam("containerId") String containerId) {
        logger.info("OrderQueryResource.getByContainerId(" + containerId + ")");

        Collection<QueryOrder> orders = this.orderQueryService.getOrdersByContainerId(containerId);
        return Response.ok().entity(orders).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all orders", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "text/plain")),
            @APIResponse(responseCode = "200", description = "Order found", content = @Content(mediaType = "application/json")) })
    public Response getOrders() {
        logger.info("OrderQueryResource.getOrders()");

        Collection<QueryOrder> orders = this.orderQueryService.getOrders();
        return Response.ok().entity(orders).build();
    }

}
