package ut;

import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import ibm.gse.orderms.infrastructure.events.EventEmitterTransactional;
import ibm.gse.orderms.infrastructure.events.OrderEventBase;

/**
 * Use this mockup class to emit order events
 *
 */
public class OrderEventEmitterMock implements EventEmitterTransactional {
	public OrderEventEmitterMock() {
	}

	public boolean eventEmitted = false;
	public OrderEventBase emittedEvent = null;
	public boolean failure = false;

	@Override
	public void emit(OrderEventBase event) throws Exception {
		if (this.failure) {
			this.eventEmitted = false;
			this.emittedEvent = null;
			throw new Exception("ERROR could not connect to backbone");
		} else {
			this.eventEmitted = true;
			this.emittedEvent = event;
		}

	}

	@Override
	public void emitWithOffsets(OrderEventBase event, Map<TopicPartition, OffsetAndMetadata> offsetToCommit,
			String groupID) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void safeClose() {
		// this.eventEmitted = false;
		// this.emittedEvent = null;
	}

	public OrderEventBase getEventEmitted() {
		return emittedEvent;
	}

	public void timeOutEvent() {
		// TODO Auto-generated method stub

	}
	
}
