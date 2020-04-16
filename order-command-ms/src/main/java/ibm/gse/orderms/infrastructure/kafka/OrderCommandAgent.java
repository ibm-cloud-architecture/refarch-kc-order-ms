package ibm.gse.orderms.infrastructure.kafka;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.AppRegistry;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.EventEmitterTransactional;
import ibm.gse.orderms.infrastructure.events.EventListenerTransactional;
import ibm.gse.orderms.infrastructure.events.EventBase;
import ibm.gse.orderms.infrastructure.events.order.OrderCancelledEvent;
import ibm.gse.orderms.infrastructure.events.order.OrderEvent;
import ibm.gse.orderms.infrastructure.repository.OrderCreationException;
import ibm.gse.orderms.infrastructure.repository.OrderUpdateException;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;

/**
 * OrderCommandAgent listens to create, update and reject order commands that arrives to the order-commands topic.
 * After creating, updating or rejecting and order, an event representing those actions will get published into the orders topic as a result
 * so that any other component from the overall application that is interested on orders can be aware of the changes.
 *
 * It uses the Shipping Order Repository to save and update the orders accordingly with the commands received.
 * This repository is an in-memory Map for simplicity.
 * 
 * Once the persistence is done based on the command read, it emits events for other services to consume.
 * 
 * We have implemented the consume-transform-produce loop pattern in Event Driven Architectures so that every command is processed exactly once.
 */
@ApplicationScoped
public class OrderCommandAgent implements EventListenerTransactional {

	private static final Logger logger = LoggerFactory.getLogger(OrderCommandAgent.class.getName());

	private final KafkaConsumer<String, String> orderCommandsConsumer;
	private ShippingOrderRepository orderRepository;
	private EventEmitterTransactional orderEventProducer;
	private EventEmitterTransactional errorEventProducer;

	private Duration pollTimeOut;
	private Duration closeTimeOut;
	private String schemaVersion;
	private boolean running = true;

	public OrderCommandAgent() {
		Properties properties = KafkaInfrastructureConfig.getConsumerProperties("ordercmd-command-consumer-grp","ordercmd-command-consumer", false, "earliest");
		// Using a value of read_committed ensures that we don't read any transactional messages before the transaction completes.
		properties.put("isolation.level", "read_committed");
		this.orderCommandsConsumer = new KafkaConsumer<String, String>(properties);
		this.orderRepository = AppRegistry.getInstance().shippingOrderRepository();
		this.orderCommandsConsumer.subscribe(Collections.singletonList(KafkaInfrastructureConfig.getOrderCommandTopic()));
		this.orderEventProducer = AppRegistry.getInstance().orderEventProducer();
		this.errorEventProducer = AppRegistry.getInstance().errorEventProducer();
		this.pollTimeOut = KafkaInfrastructureConfig.CONSUMER_POLL_TIMEOUT;
		this.closeTimeOut = KafkaInfrastructureConfig.CONSUMER_CLOSE_TIMEOUT;
		this.schemaVersion = KafkaInfrastructureConfig.SCHEMA_VERSION;
	}

	/**
	 * Constructor used for unit testing
	 * 
	 * @param repo
	 * @param kafka
	 * @param oee
	 */
	public OrderCommandAgent(ShippingOrderRepository repo, KafkaConsumer<String, String> kafka, EventEmitterTransactional oee, EventEmitterTransactional eep) {
		this.orderCommandsConsumer = kafka;
		this.orderRepository = repo;
		this.orderEventProducer = oee;
		this.errorEventProducer = eep;
		this.pollTimeOut = Duration.of(10, ChronoUnit.SECONDS);
		this.closeTimeOut = Duration.of(10, ChronoUnit.SECONDS);
		this.schemaVersion = "1";
	}

	/** 
	 * Get n records from the order-command topic, calculates the offset to be committed as part of the transaction
	 * and initiates the processing of the command.
	 * 
	 * In case of timeout poll will do nothing.
	 */
	public void poll() {
		// Get records from the order-command topic.
		// The kafka consumer poll api ensures liveness. The consumer sends periodic heartbeats to the server.
		ConsumerRecords<String, String> recs = this.orderCommandsConsumer.poll(this.pollTimeOut);
		for (ConsumerRecord<String, String> rec : recs) {
			logger.info("Command event received: " + rec.value());
			// -- Deserialize the record.
			OrderCommandEvent event = OrderCommandEvent.deserialize(rec.value());
			// -- Calculate the offset to commit as part of the transaction
			Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
			TopicPartition partition = new TopicPartition(KafkaInfrastructureConfig.getOrderCommandTopic(),rec.partition());
			OffsetAndMetadata oam = new OffsetAndMetadata(rec.offset()+1);
			offsetsToCommit.put(partition,oam);
			// -- Initiate processing of the command
			handleTransaction(event,offsetsToCommit);
		}
	}

	public void safeClose() {
		try {
			orderCommandsConsumer.close(this.closeTimeOut);
		} catch (Exception e) {
			logger.warn("Failed closing Consumer", e);
		}
	}

	@Override
	public void handleTransaction(EventBase event,Map<TopicPartition, OffsetAndMetadata> offsetToCommit) {

		OrderCommandEvent commandEvent = (OrderCommandEvent) event;
		logger.info("handle command event : " + commandEvent.getType());

		switch (commandEvent.getType()) {
		case OrderCommandEvent.TYPE_CREATE_ORDER:
			processOrderCreation(commandEvent,offsetToCommit);
			break;
		case OrderCommandEvent.TYPE_UPDATE_ORDER:
			processOrderUpdate(commandEvent,offsetToCommit);
			break;
		case OrderCommandEvent.TYPE_CANCEL_ORDER:
			processOrderCancellation(commandEvent,offsetToCommit);
			break;
		}
	}

	/**
	 * Handle create order command: persist order into repository and emit event
	 * 'order created' for others to consume. The order is in pending mode until
	 * others services responded with a Voyage and a Container assignment events. When
	 * the 'order created` event is generated, the read from command topic can be
	 * committed. It commits the offset only when both save to the repository and
	 * send order created events succeed.
	 * 
	 * If it is not able to persist, it does not emit event on orders topic, but
	 * emits on errors topic. The error topics is managed downstream by other
	 * component like a CLI or an automatic recovery process. The data are not lost
	 * as they are still in order command topic.
	 * 
	 * If publishing to any topic, even after retries, fails then the approach is to
	 * die and let the scheduler recreate the app, and connection to kafka may be
	 * recovered.
	 * 
	 * The order is still in pending and the offset is not committed. So it is
	 * possible to restart the app and it will reload from the last committed
	 * offset. So the data may be reprocessed, therefore the repository should do
	 * nothing in this duplicate info if it found the order already created.
	 * 
	 * @param commandEvent
	 * @param offsetToCommit
	 */
	private void processOrderCreation(OrderCommandEvent commandEvent,Map<TopicPartition, OffsetAndMetadata> offsetToCommit) {
		// CREATE THE ORDER
		ShippingOrder shippingOrder = new ShippingOrder(commandEvent.getPayload());
		shippingOrder.setStatus(ShippingOrder.PENDING_STATUS);
		try {
			// SAVE the newly created order into the DB.
			// This action MUST support repetition given that the order creation process might get repeated
			// if all the actions to perform do not succeed.
			orderRepository.addOrUpdateNewShippingOrder(shippingOrder);
		} catch (OrderCreationException e) {
			// need other components to fix this save operation: CLI / human or automatic process
			generateErrorEvent(shippingOrder,offsetToCommit);
			return;
		}
		// Create the event to be sent to the orders topic
        OrderEvent orderCreatedEvent = new OrderEvent(new Date().getTime(),OrderEvent.TYPE_ORDER_CREATED,schemaVersion,shippingOrder.toShippingOrderPayload());
        try {
			// Emit the event and consumer offsets as a transaction
			orderEventProducer.emitWithOffsets(orderCreatedEvent,offsetToCommit,"ordercmd-command-consumer-grp");
		} catch (Exception e) {
			// the order is in the repository but the app could not send to event backbone
			// consider communication with backbone as major issue
			e.printStackTrace();
			running = false; // liveness may kill this app and restart it
			return ;
		}   
	}
	
	/**
	 * For the order update 
	 * @param commandEvent
	 */
	private void processOrderUpdate(OrderCommandEvent commandEvent, Map<TopicPartition, OffsetAndMetadata> offsetToCommit) {
	    ShippingOrder shippingOrder = new ShippingOrder(commandEvent.getPayload());
        String orderID = shippingOrder.getOrderID();
        try {
        	 Optional<ShippingOrder> oco = orderRepository.getOrderByOrderID(orderID);
        	 if (oco.isPresent()) {
				// UPDATE the order
				shippingOrder.update(oco.get());
				// SAVE the updated order in the DB.
				// This action MUST support repetition given that the order update process might get repeated
				// if all the actions to perform do not succeed.
				 orderRepository.updateShippingOrder(shippingOrder);
				 // Create the event to be sent to the orders topic
        		 OrderEvent orderUpdateEvent = new OrderEvent(new Date().getTime(),OrderEvent.TYPE_ORDER_UPDATED,schemaVersion,shippingOrder.toShippingOrderPayload());
        		 try {
					 // Emit the event and consumer offsets as a transaction
	        		 orderEventProducer.emitWithOffsets(orderUpdateEvent,offsetToCommit,"ordercmd-command-consumer-grp");
        		 } catch (Exception e) {
        			e.printStackTrace();
        			running = false; // liveness may kill this app and restart it
        		    return ;
        		 }
        	 } else {
        		logger.error("Cannot update order - Unknown order Id " + orderID);
        		generateErrorEvent(shippingOrder,offsetToCommit);
        	 }
        } catch (OrderUpdateException oue) {
        	generateErrorEvent(shippingOrder,offsetToCommit);
        }
	}

	/**
	 * For the order reject
	 * @param commandEvent
	 */
	private void processOrderCancellation(OrderCommandEvent commandEvent, Map<TopicPartition, OffsetAndMetadata> offsetToCommit) {
	    ShippingOrder shippingOrder = new ShippingOrder(commandEvent.getPayload());
        String orderID = shippingOrder.getOrderID();
        try {
        	Optional<ShippingOrder> oco = orderRepository.getOrderByOrderID(orderID);
        	if (oco.isPresent()) {
				// Update the order so that containerID and voyageID are preserved
				shippingOrder.update(oco.get());
				// Set status to cancelled
				shippingOrder.cancelOrder();
				// SAVE the updated order in the DB.
				// This action MUST support repetition given that the order update process might get repeated
				// if all the actions to perform do not succeed.
				orderRepository.updateShippingOrder(shippingOrder);
				// Create the event to be sent to the orders topic
				OrderCancelledEvent orderCancelledEvent = new OrderCancelledEvent(new Date().getTime(), schemaVersion, shippingOrder.toOrderCancelAndRejectPayload("Cancel order command received"));
        		try {
					// Emit the event and consumer offsets as a transaction
	        		orderEventProducer.emitWithOffsets(orderCancelledEvent,offsetToCommit,"ordercmd-command-consumer-grp");
        		} catch (Exception e) {
        			e.printStackTrace();
        			running = false; // liveness may kill this app and restart it
        		    return ;
        		}
        	} else {
        		logger.error("Cannot cancel order - Unknown order Id " + orderID);
        		generateErrorEvent(shippingOrder,offsetToCommit);
        	}
        } catch (OrderUpdateException oue) {
        	generateErrorEvent(shippingOrder,offsetToCommit);
        }
	}

	public boolean isRunning() {
		return this.running;
	}
	
	private void generateErrorEvent(ShippingOrder shippingOrder, Map<TopicPartition, OffsetAndMetadata> offsetToCommit) {
		// Create the event to be sent to the error topic
		ErrorEvent errorEvent = new ErrorEvent(new Date().getTime(), schemaVersion, shippingOrder.toShippingOrderPayload(),"Repository access issue");
		try {
			// Emit the event and consumer offsets as a transaction
			errorEventProducer.emitWithOffsets(errorEvent,offsetToCommit,"ordercmd-command-consumer-grp");
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error("Error event production error for order: " + shippingOrder.getOrderID());
		}
	}
}
