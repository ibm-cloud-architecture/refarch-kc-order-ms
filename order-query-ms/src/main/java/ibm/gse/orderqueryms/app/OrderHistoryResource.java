package ibm.gse.orderqueryms.app;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderqueryms.domain.model.order.history.OrderHistory;
import ibm.gse.orderqueryms.infrastructure.repository.OrderHistoryDAO;
import ibm.gse.orderqueryms.infrastructure.repository.OrderHistoryDAOMock;

@Path("orders")
public class OrderHistoryResource { //implements EventListener
	
	static final Logger logger = LoggerFactory.getLogger(OrderHistoryResource.class);

    private OrderHistoryDAO orderHistoryDAO;

    public OrderHistoryResource() {
    	orderHistoryDAO = OrderHistoryDAOMock.instance();
    }
	
	@GET
    @Path("orderHistory/{orderId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Order history by order ID", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Order history found", content = @Content(mediaType = "application/json")) })
    public Response getOrderHistory(@PathParam("orderId") String orderId) {
        logger.info("OrderHistoryResource.getOrderHistory(" + orderId + ")");
        Collection<OrderHistory> orderAction = orderHistoryDAO.getOrderStatus(orderId);
        return Response.ok(orderAction, MediaType.APPLICATION_JSON).build();
    }

	/*
	@Override
	public void handle(Event event, String event_type) {
		String orderID;
        Optional<OrderActionInfo> oqo;
        
        String containerID;
        Optional<OrderActionInfo> oqc;
        
        try {
        	if(event_type.equals("order")){
        		OrderEvent orderEvent = (OrderEvent) event;
                if(orderEvent!=null){
                	System.out.println("@@@@ in handle order action handling order" + new Gson().toJson(orderEvent));
                    switch (orderEvent.getType()) {
                    case OrderEvent.TYPE_CREATED:
                        synchronized (orderActionDAO) {
                            Order o1 = ((CreateOrderEvent) orderEvent).getPayload();
                            long timestampMillis = ((CreateOrderEvent) orderEvent).getTimestampMillis();
                            String action = ((CreateOrderEvent) orderEvent).getType();
                            OrderActionInfo orderActionItem = OrderActionInfo.newFromOrder(o1);
                            OrderAction orderAction = OrderAction.newFromOrder(orderActionItem, timestampMillis, action);
                            orderActionDAO.addOrder(orderAction);
                            orderActionDAO.orderHistory(orderAction);
                        }
                        break;
                    case OrderEvent.TYPE_UPDATED:
                        synchronized (orderActionDAO) {
                            Order o2 = ((UpdateOrderEvent) orderEvent).getPayload();
                            long timestampMillis = ((UpdateOrderEvent) orderEvent).getTimestampMillis();
                            String action = ((UpdateOrderEvent) orderEvent).getType();
                            orderID = o2.getOrderID();
                            oqo = orderActionDAO.getByOrderId(orderID);
                            if (oqo.isPresent()) {
                            	OrderActionInfo orderActionItem = oqo.get();
                            	orderActionItem.update(o2);
                            	OrderAction cqo = OrderAction.newFromOrder(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateOrder(cqo);
                            	orderActionDAO.orderHistory(cqo);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                            }
                        }
                        break;
                    case OrderEvent.TYPE_ASSIGNED:
                        synchronized (orderActionDAO) {
                            VoyageAssignment voyageAssignment = ((AssignOrderEvent) orderEvent).getPayload();
                            long timestampMillis = ((AssignOrderEvent) orderEvent).getTimestampMillis();
                            String action = ((AssignOrderEvent) orderEvent).getType();
                            orderID = voyageAssignment.getOrderID();
                            oqo = orderActionDAO.getByOrderId(orderID);
                            if (oqo.isPresent()) {
                            	OrderActionInfo orderActionItem = oqo.get();
                            	orderActionItem.assign(voyageAssignment);
                            	OrderAction orderAction = OrderAction.newFromOrder(orderActionItem, timestampMillis, action);
                                orderActionDAO.updateOrder(orderAction);
                                orderActionDAO.orderHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                            }
                        }
                        break;
                    case OrderEvent.TYPE_REJECTED:
                        synchronized (orderActionDAO) {
                            Rejection rejection = ((RejectOrderEvent) orderEvent).getPayload();
                            long timestampMillis = ((RejectOrderEvent) orderEvent).getTimestampMillis();
                            String action = ((RejectOrderEvent) orderEvent).getType();
                            orderID = rejection.getOrderID();
                            oqo = orderActionDAO.getByOrderId(orderID);
                            if (oqo.isPresent()) {
                            	OrderActionInfo orderActionItem = oqo.get();
                            	orderActionItem.reject(rejection);
                            	OrderAction orderAction = OrderAction.newFromOrder(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateOrder(orderAction);
                            	orderActionDAO.orderHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                            }
                        }
                        break;
                    case OrderEvent.TYPE_CONTAINER_ALLOCATED:
                        synchronized (orderActionDAO) {
                        	ContainerAssignment container = ((AssignContainerEvent) orderEvent).getPayload();
                        	long timestampMillis = ((AssignContainerEvent) orderEvent).getTimestampMillis();
                        	String action = ((AssignContainerEvent) orderEvent).getType();
                            orderID = container.getOrderID();
                            oqo = orderActionDAO.getByOrderId(orderID);
                            if (oqo.isPresent()) {
                            	OrderActionInfo orderActionItem = oqo.get();
                            	orderActionItem.assignContainer(container);
                            	OrderAction orderAction = OrderAction.newFromOrder(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateOrder(orderAction);
                            	orderActionDAO.orderHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                            }
                        }
                        break;
                    case OrderEvent.TYPE_CONTAINER_DELIVERED:
                        synchronized (orderActionDAO) {
                        	ContainerAssignment container = ((ContainerDeliveredEvent) orderEvent).getPayload();
                        	long timestampMillis = ((ContainerDeliveredEvent) orderEvent).getTimestampMillis();
                        	String action = ((ContainerDeliveredEvent) orderEvent).getType();
                            orderID = container.getOrderID();
                            oqo = orderActionDAO.getByOrderId(orderID);
                            if (oqo.isPresent()) {
                            	OrderActionInfo orderActionItem = oqo.get();
                            	orderActionItem.containerDelivered(container);
                            	OrderAction orderAction = OrderAction.newFromOrder(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateOrder(orderAction);
                            	orderActionDAO.orderHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                            }
                        }
                        break;
                    case OrderEvent.TYPE_CANCELLED:
                        synchronized (orderActionDAO) {
                            Cancellation cancellation = ((CancelOrderEvent) orderEvent).getPayload();
                            long timestampMillis = ((CancelOrderEvent) orderEvent).getTimestampMillis();
                            String action = ((CancelOrderEvent) orderEvent).getType();
                            orderID = cancellation.getOrderID();
                            oqo = orderActionDAO.getByOrderId(orderID);
                            if (oqo.isPresent()) {
                            	OrderActionInfo orderActionItem = oqo.get();
                            	orderActionItem.cancel(cancellation);
                            	OrderAction orderAction = OrderAction.newFromOrder(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateOrder(orderAction);
                            	orderActionDAO.orderHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                            }
                        }
                        break;
                    case OrderEvent.TYPE_COMPLETED:
                        synchronized (orderActionDAO) {
                            Order order = ((OrderCompletedEvent) orderEvent).getPayload();
                            long timestampMillis = ((OrderCompletedEvent) orderEvent).getTimestampMillis();
                            String action = ((OrderCompletedEvent) orderEvent).getType();
                            orderID = order.getOrderID();
                            oqo = orderActionDAO.getByOrderId(orderID);
                            if (oqo.isPresent()) {
                            	OrderActionInfo orderActionItem = oqo.get();
                            	orderActionItem.orderCompleted(order);
                            	OrderAction orderAction = OrderAction.newFromOrder(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateOrder(orderAction);
                            	orderActionDAO.orderHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
                            }
                        }
                        break;
                    default:
                        logger.warn("Unknown event type: " + orderEvent);
                    }
                }
        	}
        	else{
        		ContainerEvent containerEvent = (ContainerEvent) event;
                if(containerEvent!=null){
                	System.out.println("@@@@ in handle container" + new Gson().toJson(containerEvent));
                    switch (containerEvent.getType()) {
                    case ContainerEvent.TYPE_CONTAINER_ADDED:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerAddedEvent) containerEvent).getPayload();
                            long timestampMillis = ((ContainerAddedEvent) containerEvent).getTimestampMillis();
                            String action = ((ContainerAddedEvent) containerEvent).getType();
                            OrderActionInfo orderActionItem = OrderActionInfo.newFromContainer(container);
                            OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            orderActionDAO.addContainer(orderAction);
                            orderActionDAO.containerHistory(orderAction);
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_REMOVED:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerRemovedEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerRemovedEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerRemovedEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerRemoved(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_AT_LOCATION:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerAtLocationEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerAtLocationEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerAtLocationEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerAtLocation(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_ON_MAINTENANCE:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerOnMaintainanceEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerOnMaintainanceEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerOnMaintainanceEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerOnMaintainance(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_OFF_MAINTENANCE:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerOffMaintainanceEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerOffMaintainanceEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerOffMaintainanceEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerOffMaintainance(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_ORDER_ASSIGNED:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerOrderAssignedEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerOrderAssignedEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerOrderAssignedEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerOrderAssignment(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_ORDER_RELEASED:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerOrderReleasedEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerOrderReleasedEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerOrderReleasedEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerOrderReleased(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_GOODS_LOADED:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerGoodsLoadedEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerGoodsLoadedEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerGoodsLoadedEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerGoodsLoaded(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_GOOD_UNLOADED:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerGoodsUnLoadedEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerGoodsUnLoadedEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerGoodsUnLoadedEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerGoodsUnloaded(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_ON_SHIP:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerOnShipEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerOnShipEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerOnShipEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerOnShip(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_OFF_SHIP:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerOffShipEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerOffShipEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerOffShipEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerOffShip(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_ON_TRUCK:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerOnTruckEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerOnTruckEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerOnTruckEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerOnTruck(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    case ContainerEvent.TYPE_CONTAINER_OFF_TRUCK:
                        synchronized (orderActionDAO) {
                        	Container container = ((ContainerOffTruckEvent) containerEvent).getPayload();
                        	long timestampMillis = ((ContainerOffTruckEvent) containerEvent).getTimestampMillis();
                        	String action = ((ContainerOffTruckEvent) containerEvent).getType();
                            containerID = container.getContainerID();
                            oqc = orderActionDAO.getByContainerId(containerID);
                            if (oqc.isPresent()) {
                            	OrderActionInfo orderActionItem = oqc.get();
                            	orderActionItem.containerOffTruck(container);
                            	OrderAction orderAction = OrderAction.newFromContainer(orderActionItem, timestampMillis, action);
                            	orderActionDAO.updateContainer(orderAction);
                            	orderActionDAO.containerHistory(orderAction);
                            } else {
                                throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                            }
                        }
                        break;
                    default:
                        logger.warn("Unknown event type: " + containerEvent);
                    }
                }
        	}
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
		
	}
	*/

}
