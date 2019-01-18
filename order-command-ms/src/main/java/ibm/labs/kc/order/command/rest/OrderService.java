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

import ibm.labs.kc.order.command.dao.OrderDAO;
import ibm.labs.kc.order.command.dao.OrderDAOMock;
import ibm.labs.kc.order.command.dto.CreateOrderRequest;
import ibm.labs.kc.order.command.kafka.OrderProducer;
import ibm.labs.kc.order.command.model.Order;

@Path("orders")
public class OrderService {
    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    private OrderDAO orderDAO;
    private OrderProducer orderProducer;

    public OrderService() {
        orderDAO = OrderDAOMock.instance();
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

        Order order = new Order(UUID.randomUUID().toString(), cor.getProductID(), cor.getQuantity(),
                cor.getExpectedDeliveryDate(), Order.CREATED_STATE,cor.getCustomerID());
        order.setDestinationAddress(cor.getDestinationAddress());
        order.setExpectedDeliveryDate(cor.getExpectedDeliveryDate());

        //Q : store and publish or viceversa ?
        //Q : what if publish fails ?
        
        orderDAO.add(order);
        try {
            orderProducer.publish(order);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fail to publish order created event", e);
            return Response.serverError().build();
        }

        return Response.ok().entity(order).build();
    }
}