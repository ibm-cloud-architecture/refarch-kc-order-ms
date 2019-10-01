package ibm.gse.orderms.app;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.app.dto.ShippingOrderDTO;
import ibm.gse.orderms.app.dto.ShippingOrderReference;
import ibm.gse.orderms.app.dto.ShippingOrderUpdateParameters;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.domain.service.ShippingOrderFactory;
import ibm.gse.orderms.domain.service.ShippingOrderService;
import ibm.gse.orderms.infrastructure.command.events.UpdateOrderEvent;
import ibm.gse.orderms.infrastructure.events.OrderEvent;

/**
 * Expose the commands used by external client as API
 * 
 * @author jerome boyer
 *
 */
@Path("orders")
public class ShippingOrderResource {
	static final Logger logger = LoggerFactory.getLogger(ShippingOrderResource.class);
	
	@Inject
	public ShippingOrderService shippingOrderService;

	public ShippingOrderResource() {	
	}
	
	public ShippingOrderResource(ShippingOrderService shippingOrderService) {
		this.shippingOrderService = shippingOrderService;
	}

	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Request to create an order", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "400", description = "Bad create order request", content = @Content(mediaType = "text/plain")),
            @APIResponse(responseCode = "200", description = "Order created", content = @Content(mediaType = "application/json")) })
	public Response createOrder(ShippingOrderCreateParameters dto) {
		
		try {
		   ShippingOrderCreateParameters.validateInputData(dto);
		 
		} catch(IllegalArgumentException iae) {
			return Response.status(400, iae.getMessage()).build();
		}
		ShippingOrder order = ShippingOrderFactory.createNewShippingOrder(dto);
		try {
			shippingOrderService.createOrder(order);
		} catch(Exception e) {
			return Response.serverError().build();
		}
	    return Response.ok().entity(order.getOrderID()).build();
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
    public Response update(@PathParam("Id") String orderID, ShippingOrderUpdateParameters dto) {

        if(! Objects.equals(orderID, dto.getOrderID())) {
            return Response.status(404, "OrderID in body does not match PUT path").build();
        }

        Optional<ShippingOrderDTO> existingOrder = shippingOrderService.getByID(orderID);
        if (existingOrder.isPresent()) {
            ShippingOrderUpdateParameters.validate(dto, existingOrder.get());

            ShippingOrder updatedOrder = new ShippingOrder(orderID,
                    dto.getProductID(),
                    dto.getCustomerID(),
                    dto.getQuantity(),
                    dto.getPickupAddress(), dto.getPickupDate(),
                    dto.getDestinationAddress(), dto.getExpectedDeliveryDate(),
                    dto.getStatus());
            
            try {
            	shippingOrderService.updateShippingOrder(updatedOrder);
            } catch (Exception e) {
                logger.error("Fail to publish order updated event", e);
                return Response.serverError().build();
            }

            return Response.ok().entity(updatedOrder).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
    
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns all orders", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")) })
    public Response getAll() {
        logger.info("OrderAdminService.getAll()");

        Collection<ShippingOrderReference> orders = shippingOrderService.getOrderReferences();
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

        Optional<ShippingOrderDTO> oo = shippingOrderService.getByID(orderId);
        if (oo.isPresent()) {
            return Response.ok().entity(oo.get()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
