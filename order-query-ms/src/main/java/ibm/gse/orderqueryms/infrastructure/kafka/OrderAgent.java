package ibm.gse.orderqueryms.infrastructure.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ibm.gse.orderqueryms.app.AppRegistry;
import ibm.gse.orderqueryms.domain.model.ContainerAssignment;
import ibm.gse.orderqueryms.domain.model.Order;
import ibm.gse.orderqueryms.domain.model.CancelAndRejectPayload;
import ibm.gse.orderqueryms.domain.model.Spoil;
import ibm.gse.orderqueryms.domain.model.VoyageAssignment;
import ibm.gse.orderqueryms.domain.model.order.QueryOrder;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistory;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistoryInfo;
import ibm.gse.orderqueryms.infrastructure.events.AbstractEvent;
import ibm.gse.orderqueryms.infrastructure.events.EventListener;
import ibm.gse.orderqueryms.infrastructure.events.order.AssignContainerEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.AssignOrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.CancelOrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.CreateOrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.OrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.RejectOrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.SpoilOrderEvent;
import ibm.gse.orderqueryms.infrastructure.events.order.UpdateOrderEvent;
import ibm.gse.orderqueryms.infrastructure.repository.OrderDAO;
import ibm.gse.orderqueryms.infrastructure.repository.OrderHistoryDAO;

public class OrderAgent implements EventListener {

	private static final Logger logger = LoggerFactory.getLogger(OrderAgent.class.getName());

	private final KafkaConsumer<String, String> kafkaConsumer;
	private final OrderDAO orderRepository;
	private final OrderHistoryDAO orderHistoryRepository;

	public OrderAgent() {
		Properties properties = ApplicationConfig.getOrderConsumerProperties("orderquery-orders-consumer");
		kafkaConsumer = new KafkaConsumer<String, String>(properties);
		this.kafkaConsumer.subscribe(Collections.singletonList(ApplicationConfig.getOrderTopic()));

		orderRepository = AppRegistry.getInstance().orderRepository();
		orderHistoryRepository = AppRegistry.getInstance().orderHistoryRepository();
	}

	public OrderAgent(KafkaConsumer<String, String> kafkaConsumer, OrderDAO orderRepository, OrderHistoryDAO orderHistoryRepository) {
		this.kafkaConsumer = kafkaConsumer;
		this.orderRepository = orderRepository;
		this.orderHistoryRepository = orderHistoryRepository;
	}

	public List<OrderEvent> poll() {
		ConsumerRecords<String, String> recs = kafkaConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
		List<OrderEvent> result = new ArrayList<>();
		for (ConsumerRecord<String, String> rec : recs) {
			OrderEvent event = OrderEvent.deserialize(rec.value());
			result.add(event);
		}
		return result;
	}

	public void safeClose() {
		try {
			kafkaConsumer.close(ApplicationConfig.CONSUMER_CLOSE_TIMEOUT);
		} catch (Exception e) {
			logger.warn("Failed closing Consumer", e);
		}
	}

	@Override
	public void handle(AbstractEvent orderEvent) {
		try {
			logger.info("@@@@ in handle " + new Gson().toJson(orderEvent));

			// Processing current order state
			if (orderEvent instanceof OrderEvent) {
				orderEvent = (OrderEvent) orderEvent;

				String orderID;
				Optional<QueryOrder> orderQueryObject;

				if (orderEvent != null) {
					System.out.println("@@@@ in handle " + new Gson().toJson(orderEvent));
					switch (orderEvent.getType()) {
					case OrderEvent.TYPE_CREATED:
						synchronized (orderRepository) {
							Order o1 = ((CreateOrderEvent) orderEvent).getPayload();
							QueryOrder qo = QueryOrder.newFromOrder(o1);
							orderRepository.add(qo);
						}
						break;
					case OrderEvent.TYPE_UPDATED:
						synchronized (orderRepository) {
							Order o2 = ((UpdateOrderEvent) orderEvent).getPayload();
							orderID = o2.getOrderID();
							orderQueryObject = orderRepository.getById(orderID);
							if (orderQueryObject.isPresent()) {
								QueryOrder qo = orderQueryObject.get();
								qo.update(o2);
								orderRepository.update(qo);
							} else {
								throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
							}
						}
						break;
					case OrderEvent.TYPE_ASSIGNED:
						synchronized (orderRepository) {
							VoyageAssignment voyageAssignment = ((AssignOrderEvent) orderEvent).getPayload();
							orderID = voyageAssignment.getOrderID();
							orderQueryObject = orderRepository.getById(orderID);
							if (orderQueryObject.isPresent()) {
								QueryOrder qo = orderQueryObject.get();
								qo.assignVoyage(voyageAssignment);
								orderRepository.update(qo);
							} else {
								throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
							}
						}
						break;
					case OrderEvent.TYPE_REJECTED:
						synchronized (orderRepository) {
							CancelAndRejectPayload rejectionPayload = ((RejectOrderEvent) orderEvent).getPayload();
							orderID = rejectionPayload.getOrderID();
							orderQueryObject = orderRepository.getById(orderID);
							if (orderQueryObject.isPresent()) {
								QueryOrder qo = orderQueryObject.get();
								qo.reject(rejectionPayload);
								orderRepository.update(qo);
							} else {
								throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
							}
						}
						break;
					case OrderEvent.TYPE_CONTAINER_ALLOCATED:
						synchronized (orderRepository) {
							ContainerAssignment container = ((AssignContainerEvent) orderEvent).getPayload();
							orderID = container.getOrderID();
							orderQueryObject = orderRepository.getById(orderID);
							if (orderQueryObject.isPresent()) {
								QueryOrder qo = orderQueryObject.get();
								qo.assignContainer(container);
								orderRepository.update(qo);
							} else {
								throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
							}
						}
						break;
					case OrderEvent.TYPE_CANCELLED:
						synchronized (orderRepository) {
							CancelAndRejectPayload cancellationPayload = ((CancelOrderEvent) orderEvent).getPayload();
							orderID = cancellationPayload.getOrderID();
							orderQueryObject = orderRepository.getById(orderID);
							if (orderQueryObject.isPresent()) {
								QueryOrder qo = orderQueryObject.get();
								qo.cancel(cancellationPayload);
								orderRepository.update(qo);
							} else {
								throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
							}
						}
						break;
					case OrderEvent.TYPE_SPOILT:
						synchronized (orderRepository) {
							Spoil spoil = ((SpoilOrderEvent) orderEvent).getPayload();
							orderID = spoil.getOrderID();
							orderQueryObject = orderRepository.getById(orderID);
							if (orderQueryObject.isPresent()) {
								QueryOrder qo = orderQueryObject.get();
								qo.spoilOrder(spoil);
								orderRepository.update(qo);
							} else {
								throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
							}
						}
						break;
					default:
						logger.warn("Unknown event type: " + orderEvent);
					}
				}

				/*
				 * Below is from OrderActionService... How does this merge with above?
				 */

				// Processing order history

		        Optional<OrderHistoryInfo> orderHistoryInfo; // was oqo
				
				System.out.println("@@@@ in handle order action handling order" + new Gson().toJson(orderEvent));
				switch (orderEvent.getType()) {
				case OrderEvent.TYPE_CREATED:
					synchronized (orderHistoryRepository) {
						Order o1 = ((CreateOrderEvent) orderEvent).getPayload();
						long timestampMillis = ((CreateOrderEvent) orderEvent).getTimestampMillis();
						String action = ((CreateOrderEvent) orderEvent).getType();
						OrderHistoryInfo orderActionItem = OrderHistoryInfo.newFromOrder(o1);
						OrderHistory orderAction = OrderHistory.newFromOrder(orderActionItem, timestampMillis, action);
						orderHistoryRepository.addOrder(orderAction);
						orderHistoryRepository.orderHistory(orderAction);
					}
					break;
				case OrderEvent.TYPE_UPDATED:
					synchronized (orderHistoryRepository) {
						Order o2 = ((UpdateOrderEvent) orderEvent).getPayload();
						long timestampMillis = ((UpdateOrderEvent) orderEvent).getTimestampMillis();
						String action = ((UpdateOrderEvent) orderEvent).getType();
						orderID = o2.getOrderID();
						orderHistoryInfo = orderHistoryRepository.getByOrderId(orderID);
						if (orderHistoryInfo.isPresent()) {
							OrderHistoryInfo orderActionItem = orderHistoryInfo.get();
							orderActionItem.update(o2);
							OrderHistory cqo = OrderHistory.newFromOrder(orderActionItem, timestampMillis, action);
							orderHistoryRepository.updateOrder(cqo);
							orderHistoryRepository.orderHistory(cqo);
						} else {
							throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
						}
					}
					break;
				case OrderEvent.TYPE_ASSIGNED:
					synchronized (orderHistoryRepository) {
						VoyageAssignment voyageAssignment = ((AssignOrderEvent) orderEvent).getPayload();
						long timestampMillis = ((AssignOrderEvent) orderEvent).getTimestampMillis();
						String action = ((AssignOrderEvent) orderEvent).getType();
						orderID = voyageAssignment.getOrderID();
						orderHistoryInfo = orderHistoryRepository.getByOrderId(orderID);
						if (orderHistoryInfo.isPresent()) {
							OrderHistoryInfo orderActionItem = orderHistoryInfo.get();
							orderActionItem.assignVoyage(voyageAssignment);
							OrderHistory orderAction = OrderHistory.newFromOrder(orderActionItem, timestampMillis,
									action);
							orderHistoryRepository.updateOrder(orderAction);
							orderHistoryRepository.orderHistory(orderAction);
						} else {
							throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
						}
					}
					break;
				case OrderEvent.TYPE_REJECTED:
					synchronized (orderHistoryRepository) {
						CancelAndRejectPayload rejectionPayload = ((RejectOrderEvent) orderEvent).getPayload();
						long timestampMillis = ((RejectOrderEvent) orderEvent).getTimestampMillis();
						String action = ((RejectOrderEvent) orderEvent).getType();
						orderID = rejectionPayload.getOrderID();
						orderHistoryInfo = orderHistoryRepository.getByOrderId(orderID);
						if (orderHistoryInfo.isPresent()) {
							OrderHistoryInfo orderActionItem = orderHistoryInfo.get();
							orderActionItem.reject(rejectionPayload);
							OrderHistory orderAction = OrderHistory.newFromOrder(orderActionItem, timestampMillis,
									action);
							orderHistoryRepository.updateOrder(orderAction);
							orderHistoryRepository.orderHistory(orderAction);
						} else {
							throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
						}
					}
					break;
				case OrderEvent.TYPE_CONTAINER_ALLOCATED:
					synchronized (orderHistoryRepository) {
						ContainerAssignment container = ((AssignContainerEvent) orderEvent).getPayload();
						long timestampMillis = ((AssignContainerEvent) orderEvent).getTimestampMillis();
						String action = ((AssignContainerEvent) orderEvent).getType();
						orderID = container.getOrderID();
						orderHistoryInfo = orderHistoryRepository.getByOrderId(orderID);
						if (orderHistoryInfo.isPresent()) {
							OrderHistoryInfo orderActionItem = orderHistoryInfo.get();
							orderActionItem.assignContainer(container);
							OrderHistory orderAction = OrderHistory.newFromOrder(orderActionItem, timestampMillis,
									action);
							orderHistoryRepository.updateOrder(orderAction);
							orderHistoryRepository.orderHistory(orderAction);
						} else {
							throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
						}
					}
					break;
				case OrderEvent.TYPE_CANCELLED:
					synchronized (orderHistoryRepository) {
						CancelAndRejectPayload cancellationPayload = ((CancelOrderEvent) orderEvent).getPayload();
						long timestampMillis = ((CancelOrderEvent) orderEvent).getTimestampMillis();
						String action = ((CancelOrderEvent) orderEvent).getType();
						orderID = cancellationPayload.getOrderID();
						orderHistoryInfo = orderHistoryRepository.getByOrderId(orderID);
						if (orderHistoryInfo.isPresent()) {
							OrderHistoryInfo orderActionItem = orderHistoryInfo.get();
							orderActionItem.cancel(cancellationPayload);
							OrderHistory orderAction = OrderHistory.newFromOrder(orderActionItem, timestampMillis,
									action);
							orderHistoryRepository.updateOrder(orderAction);
							orderHistoryRepository.orderHistory(orderAction);
						} else {
							throw new IllegalStateException("Cannot update - Unknown order Id " + orderID);
						}
					}
					break;
				case OrderEvent.TYPE_SPOILT:
					synchronized (orderHistoryRepository) {
						Spoil spoil = ((SpoilOrderEvent) orderEvent).getPayload();
						long timestampMillis = ((SpoilOrderEvent) orderEvent).getTimestampMillis();
						String action = ((SpoilOrderEvent) orderEvent).getType();
						orderID = spoil.getOrderID();
						orderHistoryInfo = orderHistoryRepository.getByOrderId(orderID);
						if (orderHistoryInfo.isPresent()) {
							OrderHistoryInfo orderActionItem = orderHistoryInfo.get();
							orderActionItem.spoil(spoil);
							OrderHistory orderAction = OrderHistory.newFromOrder(orderActionItem, timestampMillis,
									action);
							orderHistoryRepository.updateOrder(orderAction);
							orderHistoryRepository.orderHistory(orderAction);
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
			logger.error((new Date()).toString() + " " + e.getMessage(), e);
		}

	}

}
