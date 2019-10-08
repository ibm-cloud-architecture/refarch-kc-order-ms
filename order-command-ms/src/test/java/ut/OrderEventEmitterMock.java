package ut;

import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.events.OrderEventBase;

/**
 * Use this mockup class to emit order events
 *
 */
public class OrderEventEmitterMock implements EventEmitter{
	public OrderEventEmitterMock() {}
	
	public boolean eventEmitted = false;
	public OrderEventBase emittedEvent = null;

	@Override
	public void emit(OrderEventBase event) throws Exception {
		this.eventEmitted = true;
		this.emittedEvent = event;	
	}

	@Override
	public void safeClose() {
		this.eventEmitted = false;
		this.emittedEvent = null;	
	}

	public OrderEventBase getEventEmitted() {
		return emittedEvent;
	}
	
}
