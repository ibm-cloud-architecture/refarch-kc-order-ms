package ibm.labs.kc.order.command.service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

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

import com.google.gson.Gson;

import ibm.labs.kc.order.command.dao.CommandOrder;
import ibm.labs.kc.order.command.dao.OrderDAO;
import ibm.labs.kc.order.command.dao.OrderDAOMock;
import ibm.labs.kc.order.command.model.Cancellation;
import ibm.labs.kc.order.command.model.ContainerAssignment;
import ibm.labs.kc.order.command.model.Order;
import ibm.labs.kc.order.command.model.VoyageAssignment;
import ibm.labs.kc.order.command.model.events.AssignContainerEvent;
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
        Optional<CommandOrder> oco;
        try {
            OrderEvent orderEvent = (OrderEvent) event;
            System.out.println("@@@@ in handle " + new Gson().toJson(orderEvent));
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
                    oco = orderDAO.getByID(orderID);
                    if (oco.isPresent()) {
                        CommandOrder co = oco.get();
                        co.update(o2);
                        orderDAO.update(co);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                    }
                }
                break;
            case OrderEvent.TYPE_ASSIGNED:
                synchronized (orderDAO) {
                    VoyageAssignment voyageAssignment = ((AssignOrderEvent) orderEvent).getPayload();
                    orderID = voyageAssignment.getOrderID();
                    oco = orderDAO.getByID(orderID);
                    if (oco.isPresent()) {
                        CommandOrder co = oco.get();
                        co.assign(voyageAssignment);
                        orderDAO.update(co);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                    }
                }
                break;
            case OrderEvent.TYPE_CANCELLED:
                synchronized (orderDAO) {
                    Cancellation cancellation = ((CancelOrderEvent) orderEvent).getPayload();
                    orderID = cancellation.getOrderID();
                    oco = orderDAO.getByID(orderID);
                    if (oco.isPresent()) {
                        CommandOrder co = oco.get();
                        co.cancel(cancellation);
                        orderDAO.update(co);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                    }
                }
                break;
            case OrderEvent.TYPE_CONTAINER_ALLOCATED:
            	synchronized (orderDAO) {
	            	ContainerAssignment ca = ((AssignContainerEvent) orderEvent).getPayload();
	            	orderID = ca.getOrderID();
	            	 oco = orderDAO.getByID(orderID);
	            	 if (oco.isPresent()) {
	                     CommandOrder co = oco.get();
	                     co.assignContainer(ca);
	                     orderDAO.update(co);
	                 } else {
	                     throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
	                 }
            	}
            	break;
            default:
                logger.warn("Unknown event type: " + orderEvent);
            }
        } catch (Exception e) {
            logger.error((new Date()).toString() + " " + e.getMessage(), e);
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


    @GET
    @Path("{Id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Query an order by id", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = "text/plain")),
            @APIResponse(responseCode = "200", description = "Order found", content = @Content(mediaType = "application/json")) })
    public Response getById(@PathParam("Id") String orderId) {
        logger.info("QueryService.getById(" + orderId + ")");

        Optional<CommandOrder> oo = orderDAO.getByID(orderId);
        if (oo.isPresent()) {
            return Response.ok().entity(oo.get()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
