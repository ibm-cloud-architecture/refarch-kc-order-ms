package ut.orderms.infrastructure;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import ibm.gse.orderms.infrastructure.events.OrderEvent;
import ibm.gse.orderms.infrastructure.kafka.OrderEventAgent;

public class TestOrderEventAgent {

	@Test
	/**
	 * Send order created events ...
	 */
	public void shouldDoNothing() {
		OrderEventAgent agent = new OrderEventAgent();
		List<OrderEvent> events = agent.poll();
		for (OrderEvent event : events) {
			agent.handle(event);
		}
		
	}

}
