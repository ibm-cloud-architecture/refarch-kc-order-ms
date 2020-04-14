package ibm.gse.orderms.infrastructure.events;

import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

public interface EventEmitterTransactional extends EventEmitter {

    public void emitWithOffsets(EventBase event, Map<TopicPartition, OffsetAndMetadata> offsetToCommit, String groupID) throws Exception;

}
