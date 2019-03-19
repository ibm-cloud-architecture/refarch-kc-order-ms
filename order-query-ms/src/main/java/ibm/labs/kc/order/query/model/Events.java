package ibm.labs.kc.order.query.model;

import java.util.List;

import ibm.labs.kc.order.query.model.events.ContainerEvent;
import ibm.labs.kc.order.query.model.events.OrderEvent;

public class Events {
	
	private List<OrderEvent> orderEvent;
	private List<ContainerEvent> containerEvent;
	
	public Events(List<OrderEvent> orderEvent, List<ContainerEvent> containerEvent){
		this.setOrderEvent(orderEvent);
		this.setContainerEvent(containerEvent);
	}

	public List<OrderEvent> getOrderEvent() {
		return orderEvent;
	}

	public void setOrderEvent(List<OrderEvent> orderEvent) {
		this.orderEvent = orderEvent;
	}

	public List<ContainerEvent> getContainerEvent() {
		return containerEvent;
	}

	public void setContainerEvent(List<ContainerEvent> containerEvent) {
		this.containerEvent = containerEvent;
	}

}
