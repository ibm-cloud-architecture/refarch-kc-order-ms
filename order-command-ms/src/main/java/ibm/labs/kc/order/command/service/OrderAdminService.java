package ibm.labs.kc.order.command.service;

import java.util.Collection;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.labs.kc.order.command.dao.OrderDAO;
import ibm.labs.kc.order.command.dao.OrderDAOMock;
import ibm.labs.kc.order.command.model.Cancellation;
import ibm.labs.kc.order.command.model.CommandOrder;
import ibm.labs.kc.order.command.model.Order;
import ibm.labs.kc.order.command.model.VoyageAssignment;
import ibm.labs.kc.order.command.model.events.AssignOrderEvent;
import ibm.labs.kc.order.command.model.events.CancelOrderEvent;
import ibm.labs.kc.order.command.model.events.CreateOrderEvent;
import ibm.labs.kc.order.command.model.events.Event;
import ibm.labs.kc.order.command.model.events.EventListener;
import ibm.labs.kc.order.command.model.events.OrderEvent;
import ibm.labs.kc.order.command.model.events.UpdateOrderEvent;

@Path("orders")
public class OrderAdminService implements EventListener {

    static final Logger logger = LoggerFactory.getLogger(OrderAdminService.class);
    private OrderDAO orderDAO;

    public OrderAdminService() {
        orderDAO = OrderDAOMock.instance();
    }

    @Override
    public void handle(Event event) {
        String orderID;
        Optional<CommandOrder> oqo;
        try {
            OrderEvent orderEvent = (OrderEvent) event;
            switch (orderEvent.getType()) {
            case OrderEvent.TYPE_CREATED:
                synchronized (orderDAO) {
                    Order o1 = ((CreateOrderEvent) orderEvent).getPayload();
                    orderDAO.add(CommandOrder.newFromOrder(o1));
                }
                break;
            case OrderEvent.TYPE_UPDATED:
                synchronized (orderDAO) {
                    Order o2 = ((UpdateOrderEvent) orderEvent).getPayload();
                    orderID = o2.getOrderID();
                    oqo = orderDAO.getByID(orderID);
                    if (oqo.isPresent()) {
                        CommandOrder qo = oqo.get();
                        qo.update(o2);
                        orderDAO.update(qo);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                    }
                }
                break;
            case OrderEvent.TYPE_ASSIGNED:
                synchronized (orderDAO) {
                    VoyageAssignment voyageAssignment = ((AssignOrderEvent) orderEvent).getPayload();
                    orderID = voyageAssignment.getOrderID();
                    oqo = orderDAO.getByID(orderID);
                    if (oqo.isPresent()) {
                        CommandOrder qo = oqo.get();
                        qo.assign(voyageAssignment);
                        orderDAO.update(qo);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                    }
                }
                break;
            case OrderEvent.TYPE_CANCELLED:
                synchronized (orderDAO) {
                    Cancellation cancellation = ((CancelOrderEvent) orderEvent).getPayload();
                    orderID = cancellation.getOrderID();
                    oqo = orderDAO.getByID(orderID);
                    if (oqo.isPresent()) {
                        CommandOrder qo = oqo.get();
                        qo.cancel(cancellation);
                        orderDAO.update(qo);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                    }
                }
                break;
            default:
                logger.warn("Unknown event type: " + orderEvent);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns all orders", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")) })
    public Response getAll() {
        logger.info("OrderAdminService.getAll()");

        Collection<CommandOrder> orders = orderDAO.getAll();
        return Response.ok().entity(orders).build();
    }

}
