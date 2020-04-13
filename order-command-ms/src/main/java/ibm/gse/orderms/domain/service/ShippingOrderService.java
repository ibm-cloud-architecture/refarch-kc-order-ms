package ibm.gse.orderms.domain.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderms.app.dto.ShippingOrderReference;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.AppRegistry;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.events.ShippingOrderPayload;
import ibm.gse.orderms.infrastructure.kafka.KafkaInfrastructureConfig;
import ibm.gse.orderms.infrastructure.repository.OrderCreationException;
import ibm.gse.orderms.infrastructure.repository.OrderUpdateException;
import ibm.gse.orderms.infrastructure.repository.OrderCancelException;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;

public class ShippingOrderService {
	static final Logger logger = LoggerFactory.getLogger(ShippingOrderService.class);
	   
	private EventEmitter emitter;
	private ShippingOrderRepository orderRepository = null;
	
	public ShippingOrderService() {
		this.emitter = AppRegistry.getInstance().orderCommandProducer();
		this.orderRepository = AppRegistry.getInstance().shippingOrderRepository();
	}
	
	public ShippingOrderService(EventEmitter emitter, ShippingOrderRepository orderRepository){
		this.emitter = emitter;
		this.orderRepository = orderRepository;
	}
	
	
	public void createOrder(ShippingOrderPayload shippingOrderPayload) throws OrderCreationException {
		OrderCommandEvent createOrderCommandEvent = new OrderCommandEvent(System.currentTimeMillis(), KafkaInfrastructureConfig.SCHEMA_VERSION, shippingOrderPayload, OrderCommandEvent.TYPE_CREATE_ORDER);	
		try {
            emitter.emit(createOrderCommandEvent);
		} catch (Exception e) {
			emitter.safeClose();
			throw new OrderCreationException("Error while emitting create order command event");
		} 
	}

	public Collection<ShippingOrderReference> getOrderReferences() {
		Collection<ShippingOrder> orders = this.orderRepository.getAll();
		Collection<ShippingOrderReference> orderReferences = new ArrayList<ShippingOrderReference>();
		for (ShippingOrder order : orders) {
			ShippingOrderReference ref = new ShippingOrderReference(order.getOrderID(),
					order.getCustomerID(),
					order.getProductID(),
					order.getStatus()); 
			orderReferences.add(ref);
		}
		return  orderReferences;
	}

	public Optional<ShippingOrder> getOrderByOrderID(String orderId) {	
		return this.orderRepository.getOrderByOrderID(orderId);
	}

	public void updateShippingOrder(ShippingOrderPayload shippingOrderPayload) throws OrderUpdateException {
		logger.info("updateShippingOrder "+ shippingOrderPayload.getOrderID());
         
		OrderCommandEvent updateOrderCommandEvent = new OrderCommandEvent(System.currentTimeMillis(), KafkaInfrastructureConfig.SCHEMA_VERSION, shippingOrderPayload, OrderCommandEvent.TYPE_UPDATE_ORDER);	
      	try {
            emitter.emit(updateOrderCommandEvent);
        } catch (Exception e) {
            logger.error("Fail to publish order update event", e);
            emitter.safeClose();
            throw new OrderUpdateException("Error while emitting update order command event");
        } 
	}

	public void cancelShippingOrder(ShippingOrderPayload shippingOrderPayload) throws OrderCancelException {
		logger.info("cancelShippingOrder " + shippingOrderPayload.getOrderID());
		
		OrderCommandEvent cancelOrderCommandEvent = new OrderCommandEvent(System.currentTimeMillis(), KafkaInfrastructureConfig.SCHEMA_VERSION, shippingOrderPayload, OrderCommandEvent.TYPE_CANCEL_ORDER);
		try {
			emitter.emit(cancelOrderCommandEvent);
		} catch (Exception e) {
			logger.error("Fail to publish order cancel event", e);
			emitter.safeClose();
			throw new OrderCancelException("Error while emitting cancel order command event");
		}
	}

}
