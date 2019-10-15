package ibm.gse.orderqueryms.app;

import java.util.Collection;
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

import ibm.gse.orderqueryms.domain.model.order.QueryOrder;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAO;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAOMock;

@Path("orders")
public class OrderQueryResource {
    static final Logger logger = LoggerFactory.getLogger(OrderQueryResource.class);

    private OrderDAO orderDAO;

    public OrderQueryResource() {
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
        logger.info("QueryService.getById(" + orderId + ")");

        Optional<QueryOrder> oo = orderDAO.getById(orderId);
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
        logger.info("QueryService.getByManuf(" + manuf + ")");

        Collection<QueryOrder> orders = orderDAO.getByManuf(manuf);
        return Response.ok().entity(orders).build();
    }

    @GET
    @Path("byStatus/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Query orders by status", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Orders found", content = @Content(mediaType = "application/json")) })
    public Response getByStatus(@PathParam("status") String status) {
        logger.info("QueryService.getByStatus(" + status + ")");

        Collection<QueryOrder> orders = orderDAO.getByStatus(status);
        return Response.ok().entity(orders).build();
    }

    /* TO BE REMOVED
    @Override
    public void handle(Event event, String event_type) {
        String orderID;
        Optional<QueryOrder> oqo;
        try {
            OrderEvent orderEvent = (OrderEvent) event;
            if(orderEvent!=null){
            	System.out.println("@@@@ in handle " + new Gson().toJson(orderEvent));
                switch (orderEvent.getType()) {
                case OrderEvent.TYPE_CREATED:
                    synchronized (orderDAO) {
                        Order o1 = ((CreateOrderEvent) orderEvent).getPayload();
                        QueryOrder qo = QueryOrder.newFromOrder(o1);
                        orderDAO.add(qo);
                    }
                    break;
                case OrderEvent.TYPE_UPDATED:
                    synchronized (orderDAO) {
                        Order o2 = ((UpdateOrderEvent) orderEvent).getPayload();
                        orderID = o2.getOrderID();
                        oqo = orderDAO.getById(orderID);
                        if (oqo.isPresent()) {
                            QueryOrder qo = oqo.get();
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
                        oqo = orderDAO.getById(orderID);
                        if (oqo.isPresent()) {
                            QueryOrder qo = oqo.get();
                            qo.assign(voyageAssignment);
                            orderDAO.update(qo);
                        } else {
                            throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                        }
                    }
                    break;
                case OrderEvent.TYPE_REJECTED:
                    synchronized (orderDAO) {
                        Rejection rejection = ((RejectOrderEvent) orderEvent).getPayload();
                        orderID = rejection.getOrderID();
                        oqo = orderDAO.getById(orderID);
                        if (oqo.isPresent()) {
                            QueryOrder qo = oqo.get();
                            qo.reject(rejection);
                            orderDAO.update(qo);
                        } else {
                            throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                        }
                    }
                    break;
                case OrderEvent.TYPE_CONTAINER_ALLOCATED:
                    synchronized (orderDAO) {
                    	ContainerAssignment container = ((AssignContainerEvent) orderEvent).getPayload();
                        orderID = container.getOrderID();
                        oqo = orderDAO.getById(orderID);
                        if (oqo.isPresent()) {
                            QueryOrder qo = oqo.get();
                            qo.assignContainer(container);
                            orderDAO.update(qo);
                        } else {
                            throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                        }
                    }
                    break;
                case OrderEvent.TYPE_CONTAINER_DELIVERED:
                    synchronized (orderDAO) {
                    	ContainerAssignment container = ((ContainerDeliveredEvent) orderEvent).getPayload();
                        orderID = container.getOrderID();
                        oqo = orderDAO.getById(orderID);
                        if (oqo.isPresent()) {
                            QueryOrder qo = oqo.get();
                            qo.containerDelivered(container);
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
                        oqo = orderDAO.getById(orderID);
                        if (oqo.isPresent()) {
                            QueryOrder qo = oqo.get();
                            qo.cancel(cancellation);
                            orderDAO.update(qo);
                        } else {
                            throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                        }
                    }
                    break;
                case OrderEvent.TYPE_COMPLETED:
                    synchronized (orderDAO) {
                        Order order = ((OrderCompletedEvent) orderEvent).getPayload();
                        orderID = order.getOrderID();
                        oqo = orderDAO.getById(orderID);
                        if (oqo.isPresent()) {
                            QueryOrder qo = oqo.get();
                            qo.orderCompleted(order);
                            orderDAO.update(qo);
                        } else {
                            throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                        }
                    }
                    break;
                default:
                    logger.warn("Unknown event type: " + orderEvent);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    */

}
