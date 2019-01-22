package ibm.labs.kc.order.command.service;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

import ibm.labs.kc.order.command.dao.OrderDAO;
import ibm.labs.kc.order.command.dao.OrderDAOMock;
import ibm.labs.kc.order.command.dto.OrderRequest;
import ibm.labs.kc.order.command.kafka.OrderProducer;
import ibm.labs.kc.order.command.model.EventEmitter;
import ibm.labs.kc.order.command.model.Order;
import ibm.labs.kc.order.command.model.OrderEvent;

@Path("orders")
public class OrderCRUDService {
    private static final Logger logger = Logger.getLogger(OrderCRUDService.class.getName());

    private EventEmitter emitter;
    private OrderDAO orderDAO;

    public OrderCRUDService() {
        emitter = OrderProducer.instance();
        orderDAO = OrderDAOMock.instance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Request to create an order", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "400", description = "Bad create order request", content = @Content(mediaType = "text/plain")),
            @APIResponse(responseCode = "200", description = "Order created", content = @Content(mediaType = "application/json")) })
    public Response create(OrderRequest cor) {

        OrderRequest.validate(cor);

        Order order = new Order(UUID.randomUUID().toString(), 
                cor.getProductID(),
                cor.getCustomerID(), 
                cor.getQuantity(),
                cor.getPickupAddress(), cor.getPickupDate(),
                cor.getDestinationAddress(), cor.getExpectedDeliveryDate());

        OrderEvent orderEvent = new OrderEvent(System.currentTimeMillis(),
                OrderEvent.TYPE_CREATED, "1", order);

        try {
            emitter.emit(orderEvent);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fail to publish order created event", e);
            return Response.serverError().build();
        }

        return Response.ok().entity(order).build();
    }

    @PUT
    @Path("{Id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Request to update an order", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "404", description = "Unknown order ID", content = @Content(mediaType = "text/plain")),
            @APIResponse(responseCode = "400", description = "Bad update order request", content = @Content(mediaType = "text/plain")),
            @APIResponse(responseCode = "200", description = "Order updated", content = @Content(mediaType = "application/json")) })
    public Response update(@PathParam("Id") String orderID, OrderRequest cor) {

        if (orderDAO.getByID(orderID) != null) {
            OrderRequest.validate(cor);

            Order updatedOrder = new Order(orderID, 
                    cor.getProductID(),
                    cor.getCustomerID(), 
                    cor.getQuantity(),
                    cor.getPickupAddress(), cor.getPickupDate(),
                    cor.getDestinationAddress(), cor.getExpectedDeliveryDate());
            OrderEvent orderEvent = new OrderEvent(System.currentTimeMillis(),
                    OrderEvent.TYPE_UPDATED, "1", updatedOrder);

            try {
                emitter.emit(orderEvent);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Fail to publish order updated event", e);
                return Response.serverError().build();
            }

            return Response.ok().entity(updatedOrder).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

}