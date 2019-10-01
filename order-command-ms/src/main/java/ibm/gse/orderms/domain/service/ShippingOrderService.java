package ibm.gse.orderms.domain.service;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.orderms.app.dto.ShippingOrderDTO;
import ibm.gse.orderms.app.dto.ShippingOrderReference;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.command.events.CreateOrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandEmitter;
import ibm.gse.orderms.infrastructure.repository.OrderRepository;
import ibm.gse.orderms.infrastructure.repository.OrderRepositoryMock;

public class ShippingOrderService {
	static final Logger logger = LoggerFactory.getLogger(ShippingOrderService.class);
	   
	private EventEmitter emitter;
	private OrderRepository orderRepository = null;
	
	public ShippingOrderService() {
		this.emitter = OrderCommandEmitter.instance();
		this.orderRepository = OrderRepositoryMock.instance();
	}
	
	public ShippingOrderService(EventEmitter emitter, OrderRepository orderRepository){
		this.emitter = emitter;
		this.orderRepository = orderRepository;
	}

	public void createOrder(ShippingOrder order) {
		CreateOrderCommandEvent createOrderCommandEvent = new CreateOrderCommandEvent(System.currentTimeMillis(), "1", order);
        try {
            emitter.emit(createOrderCommandEvent);
        } catch (Exception e) {
            logger.error("Fail to publish order created event", e);
        }
		
	}

	public Collection<ShippingOrderReference> getOrderReferences() {
		// TODO Auto-generated method stub
		// ShippingOrderDTO.newFromOrder(order1);
		return null;
	}

	public Optional<ShippingOrderDTO> getByID(String orderId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateShippingOrder(ShippingOrder updatedOrder) {
		// TODO Auto-generated method stub
		
	}

}
