package ibm.labs.kc.order.command.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.labs.kc.order.command.dao.OrderDAO;
import ibm.labs.kc.order.command.dao.OrderDAOMock;
import ibm.labs.kc.order.command.dto.OrderCreate;
import ibm.labs.kc.order.command.dto.OrderUpdate;
import ibm.labs.kc.order.command.kafka.OrderProducer;
import ibm.labs.kc.order.command.model.CommandOrder;
import ibm.labs.kc.order.command.model.Order;
import ibm.labs.kc.order.command.model.events.CreateOrderEvent;
import ibm.labs.kc.order.command.model.events.EventEmitter;
import ibm.labs.kc.order.command.model.events.OrderEvent;
import ibm.labs.kc.order.command.model.events.UpdateOrderEvent;

@Path("orders")
public class OrderCRUDService {
    private static final Logger logger = LoggerFactory.getLogger(OrderCRUDService.class);

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
    public Response create(OrderCreate dto) {

        OrderCreate.validate(dto);

        Order order = new Order(UUID.randomUUID().toString(),
                dto.getProductID(),
                dto.getCustomerID(),
                dto.getQuantity(),
                dto.getPickupAddress(), dto.getPickupDate(),
                dto.getDestinationAddress(), dto.getExpectedDeliveryDate(),
                Order.PENDING_STATUS);

        OrderEvent orderEvent = new CreateOrderEvent(System.currentTimeMillis(), "1", order);

        try {
            emitter.emit(orderEvent);
        } catch (Exception e) {
            logger.error("Fail to publish order created event", e);
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
    public Response update(@PathParam("Id") String orderID, OrderUpdate dto) {

        if(! Objects.equals(orderID, dto.getOrderID())) {
            throw new IllegalArgumentException("OrderID in body does not match PUT path");
        }

        Optional<CommandOrder> existingOrder = orderDAO.getByID(orderID);
        if (existingOrder.isPresent()) {
            OrderUpdate.validate(dto, existingOrder.get());

            Order updatedOrder = new Order(orderID,
                    dto.getProductID(),
                    dto.getCustomerID(),
                    dto.getQuantity(),
                    dto.getPickupAddress(), dto.getPickupDate(),
                    dto.getDestinationAddress(), dto.getExpectedDeliveryDate(),
                    Order.PENDING_STATUS);

            OrderEvent orderEvent = new UpdateOrderEvent(System.currentTimeMillis(), "1", updatedOrder);

            try {
                emitter.emit(orderEvent);
            } catch (Exception e) {
                logger.error("Fail to publish order updated event", e);
                return Response.serverError().build();
            }

            return Response.ok().entity(updatedOrder).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

}