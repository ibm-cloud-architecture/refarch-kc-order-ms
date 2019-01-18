package ibm.labs.kc.order.command.rest;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import ibm.labs.kc.order.command.kafka.OrderProducer;
import ibm.labs.kc.order.command.model.Order;
import ibm.labs.kc.order.command.model.OrderEvent;

@Path("orders")
public class OrderService {
    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    private OrderProducer orderProducer;

    public OrderService() {
        orderProducer = OrderProducer.instance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Request to create an order", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "400", description = "Bad create order request", content = @Content(mediaType = "text/plain")),
            @APIResponse(responseCode = "200", description = "Order created", content = @Content(mediaType = "application/json")) })
    public Response create(CreateOrderRequest cor) {

        CreateOrderRequest.validate(cor);

        Order order = new Order(UUID.randomUUID().toString(), 
                cor.getProductID(),
                cor.getCustomerID(), 
                cor.getQuantity(),
                cor.getPickupAddress(), cor.getPickupDate(),
                cor.getDestinationAddress(), cor.getExpectedDeliveryDate());

        OrderEvent orderEvent = new OrderEvent(System.currentTimeMillis(),
                OrderEvent.TYPE_CREATED, "1", order);
        
        try {
            orderProducer.publish(orderEvent);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fail to publish order created event", e);
            return Response.serverError().build();
        }

        return Response.ok().entity(order).build();
    }
}