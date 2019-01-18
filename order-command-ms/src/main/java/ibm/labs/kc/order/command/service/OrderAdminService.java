package ibm.labs.kc.order.command.service;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import ibm.labs.kc.order.command.dao.OrderDAO;
import ibm.labs.kc.order.command.dao.OrderDAOMock;
import ibm.labs.kc.order.command.model.Event;
import ibm.labs.kc.order.command.model.EventListener;
import ibm.labs.kc.order.command.model.Order;
import ibm.labs.kc.order.command.model.OrderEvent;

@Path("orders")
public class OrderAdminService implements EventListener {

    static final Logger logger = Logger.getLogger(OrderAdminService.class.getName());
    private static OrderAdminService instance;
    private OrderDAO orderDAO;

    public static synchronized OrderAdminService instance() {
        if (instance == null) {
            instance = new OrderAdminService();
        }
        return instance;
    }

    public OrderAdminService() {
        orderDAO = OrderDAOMock.instance();
    }

    @Override
    public void handle(Event event) {
        OrderEvent orderEvent = (OrderEvent)event;
        switch (orderEvent.getType()) {
        case OrderEvent.TYPE_CREATED:
            Order order = orderEvent.getPayload();
            orderDAO.add(order);
            break;
        default:
            logger.warning("Unknown event type: " + orderEvent);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns all orders", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")) })
    public Response getAll() {
        logger.warning("OrderAdminService.getAll()");

        Collection<Order> orders = orderDAO.getAll();
        return Response.ok().entity(orders).build();
    }

}
