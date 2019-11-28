package ibm.gse.orderqueryms.infrastructure.kafka;

import java.util.ArrayList;
import java.util.Collections;
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
import ibm.gse.orderqueryms.domain.model.Container;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistory;
import ibm.gse.orderqueryms.domain.model.order.history.OrderHistoryInfo;
import ibm.gse.orderqueryms.infrastructure.events.AbstractEvent;
import ibm.gse.orderqueryms.infrastructure.events.EventListener;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerAddedEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerAtLocationEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerGoodsLoadedEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerGoodsUnLoadedEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOffMaintenanceEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOffShipEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOffTruckEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOnMaintenanceEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOnShipEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOnTruckEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOrderAssignedEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerOrderReleasedEvent;
import ibm.gse.orderqueryms.infrastructure.events.container.ContainerRemovedEvent;
import ibm.gse.orderqueryms.infrastructure.repository.OrderHistoryDAO;

public class ContainerAgent implements EventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ContainerAgent.class.getName());

	private final KafkaConsumer<String, String> kafkaConsumer;

	private final OrderHistoryDAO orderHistoryRepository;

    public ContainerAgent() {
        Properties properties = ApplicationConfig.getContainerConsumerProperties("orderquery-container-consumer");
        kafkaConsumer = new KafkaConsumer<String, String>(properties);
		this.kafkaConsumer.subscribe(Collections.singletonList(ApplicationConfig.getContainerTopic()));

		orderHistoryRepository = AppRegistry.getInstance().orderHistoryRepository();
    }
	
	public ContainerAgent(KafkaConsumer<String, String> kafkaConsumer, OrderHistoryDAO orderHistoryRepository) {
		this.kafkaConsumer = kafkaConsumer;
		this.orderHistoryRepository = orderHistoryRepository;
	}
	
    public List<ContainerEvent> poll() {
        ConsumerRecords<String, String> recs = kafkaConsumer.poll(ApplicationConfig.CONSUMER_POLL_TIMEOUT);
        List<ContainerEvent> result = new ArrayList<>();
        for (ConsumerRecord<String, String> rec : recs) {
            ContainerEvent event = ContainerEvent.deserialize(rec.value());
            result.add(event);
        }
        return result;
    }
	
    public void safeClose() {
        try {
            kafkaConsumer.close(ApplicationConfig.CONSUMER_CLOSE_TIMEOUT);
        } catch (Exception e) {
            logger.warn("Failed closing Consumer",e);
        }
    }
	
	@Override
	public void handle(AbstractEvent event) {
        
        String containerID;
        Optional<OrderHistoryInfo> oqc;
		
		ContainerEvent containerEvent = (ContainerEvent) event;
        if(containerEvent!=null){
        	System.out.println("@@@@ in handle container" + new Gson().toJson(containerEvent));
            switch (containerEvent.getType()) {
            case ContainerEvent.TYPE_CONTAINER_ADDED:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerAddedEvent) containerEvent).getPayload();
                    long timestampMillis = ((ContainerAddedEvent) containerEvent).getTimestampMillis();
                    String action = ((ContainerAddedEvent) containerEvent).getType();
                    OrderHistoryInfo orderActionItem = OrderHistoryInfo.newFromContainer(container);
                    OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    orderHistoryRepository.addContainer(orderAction);
                    orderHistoryRepository.containerHistory(orderAction);
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_REMOVED:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerRemovedEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerRemovedEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerRemovedEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerRemoved(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_AT_LOCATION:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerAtLocationEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerAtLocationEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerAtLocationEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerAtLocation(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_ON_MAINTENANCE:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerOnMaintenanceEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerOnMaintenanceEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerOnMaintenanceEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerOnMaintainance(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_OFF_MAINTENANCE:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerOffMaintenanceEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerOffMaintenanceEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerOffMaintenanceEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerOffMaintainance(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_ORDER_ASSIGNED:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerOrderAssignedEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerOrderAssignedEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerOrderAssignedEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerOrderAssignment(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_ORDER_RELEASED:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerOrderReleasedEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerOrderReleasedEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerOrderReleasedEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerOrderReleased(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_GOODS_LOADED:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerGoodsLoadedEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerGoodsLoadedEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerGoodsLoadedEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerGoodsLoaded(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_GOOD_UNLOADED:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerGoodsUnLoadedEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerGoodsUnLoadedEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerGoodsUnLoadedEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerGoodsUnloaded(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_ON_SHIP:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerOnShipEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerOnShipEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerOnShipEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerOnShip(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_OFF_SHIP:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerOffShipEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerOffShipEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerOffShipEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerOffShip(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_ON_TRUCK:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerOnTruckEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerOnTruckEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerOnTruckEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerOnTruck(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
                    } else {
                        throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                    }
                }
                break;
            case ContainerEvent.TYPE_CONTAINER_OFF_TRUCK:
                synchronized (orderHistoryRepository) {
                	Container container = ((ContainerOffTruckEvent) containerEvent).getPayload();
                	long timestampMillis = ((ContainerOffTruckEvent) containerEvent).getTimestampMillis();
                	String action = ((ContainerOffTruckEvent) containerEvent).getType();
                    containerID = container.getContainerID();
                    oqc = orderHistoryRepository.getByContainerId(containerID);
                    if (oqc.isPresent()) {
                    	OrderHistoryInfo orderActionItem = oqc.get();
                    	orderActionItem.containerOffTruck(container);
                    	OrderHistory orderAction = OrderHistory.newFromContainer(orderActionItem, timestampMillis, action);
                    	orderHistoryRepository.updateContainer(orderAction);
                    	orderHistoryRepository.containerHistory(orderAction);
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

}
