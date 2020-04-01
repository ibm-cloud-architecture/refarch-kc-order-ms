package ibm.gse.orderms.infrastructure.events;

import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

public interface EventListenerTransactional {

    public void handleTransaction(OrderEventBase event,Map<TopicPartition, OffsetAndMetadata> offsetToCommit);

}
