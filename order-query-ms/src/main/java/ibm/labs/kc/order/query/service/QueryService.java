package ibm.labs.kc.order.query.service;

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

import ibm.labs.kc.order.query.dao.OrderDAO;
import ibm.labs.kc.order.query.dao.OrderDAOMock;
import ibm.labs.kc.order.query.dao.QueryOrder;
import ibm.labs.kc.order.query.model.Cancellation;
import ibm.labs.kc.order.query.model.Container;
import ibm.labs.kc.order.query.model.Order;
import ibm.labs.kc.order.query.model.Rejection;
import ibm.labs.kc.order.query.model.VoyageAssignment;
import ibm.labs.kc.order.query.model.events.AllocatedContainerEvent;
import ibm.labs.kc.order.query.model.events.AssignOrderEvent;
import ibm.labs.kc.order.query.model.events.CancelOrderEvent;
import ibm.labs.kc.order.query.model.events.ContainerDeliveredEvent;
import ibm.labs.kc.order.query.model.events.ContainerOffShipEvent;
import ibm.labs.kc.order.query.model.events.ContainerOnShipEvent;
import ibm.labs.kc.order.query.model.events.CreateOrderEvent;
import ibm.labs.kc.order.query.model.events.Event;
import ibm.labs.kc.order.query.model.events.EventListener;
import ibm.labs.kc.order.query.model.events.OrderCompletedEvent;
import ibm.labs.kc.order.query.model.events.OrderEvent;
import ibm.labs.kc.order.query.model.events.UpdateOrderEvent;
import ibm.labs.kc.order.query.model.events.RejectOrderEvent;

@Path("orders")
public class QueryService implements EventListener {
    static final Logger logger = LoggerFactory.getLogger(QueryService.class);

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
    
    @GET
    @Path("orderHistory/{orderId}/{customerID}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Query order history by order ID for a particular customer", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Order history found", content = @Content(mediaType = "application/json")) })
    public Response getOrderHistory(@PathParam("orderId") String orderId, @PathParam("customerID") String customerID) {
        logger.info("QueryService.getOrderHistory(" + orderId + ","+ customerID +")");
        Collection<QueryOrder> orders = orderDAO.getOrderStatus(orderId, customerID);
        return Response.ok(orders, MediaType.APPLICATION_JSON).build();
    }

    @Override
    public void handle(Event event) {
        String orderID;
        Optional<QueryOrder> oqo;
        try {
            OrderEvent orderEvent = (OrderEvent) event;
            switch (orderEvent.getType()) {
            case OrderEvent.TYPE_CREATED:
                synchronized (orderDAO) {
                    Order o1 = ((CreateOrderEvent) orderEvent).getPayload();
                    orderDAO.add(QueryOrder.newFromOrder(o1));
                    orderDAO.orderHistory(QueryOrder.newFromOrder(o1));
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
            case OrderEvent.TYPE_CONTAINER_ALLOCATED_STATUS:
                synchronized (orderDAO) {
                    Container container = ((AllocatedContainerEvent) orderEvent).getPayload();
                    orderID = container.getOrderID();
                    oqo = orderDAO.getById(orderID);
                    if (oqo.isPresent()) {
                        QueryOrder qo = oqo.get();
                        qo.allocatedContainer(container);
                        orderDAO.update(qo);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                    }
                }
                break;
            case OrderEvent.TYPE_CONTAINER_ON_SHIP_STATUS:
                synchronized (orderDAO) {
                    Container container = ((ContainerOnShipEvent) orderEvent).getPayload();
                    orderID = container.getOrderID();
                    oqo = orderDAO.getById(orderID);
                    if (oqo.isPresent()) {
                        QueryOrder qo = oqo.get();
                        qo.containerOnShip(container);
                        orderDAO.update(qo);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                    }
                }
                break;
            case OrderEvent.TYPE_CONTAINER_OFF_SHIP_STATUS:
                synchronized (orderDAO) {
                    Container container = ((ContainerOffShipEvent) orderEvent).getPayload();
                    orderID = container.getOrderID();
                    oqo = orderDAO.getById(orderID);
                    if (oqo.isPresent()) {
                        QueryOrder qo = oqo.get();
                        qo.containerOffShip(container);
                        orderDAO.update(qo);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                    }
                }
                break;
            case OrderEvent.TYPE_CONTAINER_DELIVERED_STATUS:
                synchronized (orderDAO) {
                    Container container = ((ContainerDeliveredEvent) orderEvent).getPayload();
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
            case OrderEvent.TYPE_ORDER_COMPLETED:
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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
