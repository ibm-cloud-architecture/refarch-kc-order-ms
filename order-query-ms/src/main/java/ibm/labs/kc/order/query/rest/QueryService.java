package ibm.labs.kc.order.query.rest;

import java.util.Optional;
import java.util.logging.Logger;

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

import ibm.labs.kc.order.query.dao.OrderDAO;
import ibm.labs.kc.order.query.dao.OrderDAOMock;
import ibm.labs.kc.order.query.model.Order;

@Path("orders")
public class QueryService {
    static final Logger logger = Logger.getLogger(QueryService.class.getName());

    private OrderDAO orderDAO;

    public QueryService() {
        orderDAO = OrderDAOMock.instance();
    }

    @GET
    @Path("{Id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Query an order by id", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "text/plain")),
            @APIResponse(responseCode = "200", description = "Order found", content = @Content(mediaType = "application/json")) })
    public Response getById(@PathParam("Id") String orderId) {
        logger.warning("QueryService.getById" + orderId);

        Optional<Order> oo = orderDAO.getById(orderId);
        if (oo.isPresent()) {
            Order order = oo.get();
            return Response.ok().entity(order).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
