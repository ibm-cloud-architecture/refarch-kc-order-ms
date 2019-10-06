package ut;

import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.events.OrderEventAbstract;

/**
 * Use this mockup class to emit order events
 *
 */
public class OrderEventEmitterMock implements EventEmitter{
	public OrderEventEmitterMock() {}
	
	public boolean eventEmitted = false;
	public OrderEventAbstract emittedEvent = null;

	@Override
	public void emit(OrderEventAbstract event) throws Exception {
		this.eventEmitted = true;
		this.emittedEvent = event;	
	}

	@Override
	public void safeClose() {
		this.eventEmitted = false;
		this.emittedEvent = null;	
	}

	public OrderEventAbstract getEventEmitted() {
		return emittedEvent;
	}
	
}
