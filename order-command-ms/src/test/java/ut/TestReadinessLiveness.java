package ut;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ibm.gse.orderms.domain.events.command.OrderCommandEvent;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.kafka.KafkaInfrastructureConfig;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.kafka.OrderEventAgent;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;
import ibm.gse.orderms.infrastructure.resource.StarterLivenessCheck;
import ibm.gse.orderms.infrastructure.resource.StarterReadinessCheck;

public class TestReadinessLiveness {

	static OrderEventEmitterMock orderEventProducerMock;
	static KafkaConsumerMockup consumerMock;

	static StarterLivenessCheck liveness;
	static StarterReadinessCheck readiness;
	static OrderCommandAgent commandAgent;

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		Properties properties = ShippingOrderTestDataFactory.buildConsumerKafkaProperties();
		consumerMock = new KafkaConsumerMockup<String,String>(properties,"order-commands");	
		orderEventProducerMock = new OrderEventEmitterMock();
		ShippingOrderRepository repository = new ShippingOrderRepositoryMock();
		OrderEventEmitterMock errorEventProducerMock = new OrderEventEmitterMock();
		KafkaInfrastructureConfig config = mock(KafkaInfrastructureConfig.class);
		when (config.getOrderCommandTopic()).thenReturn("order-command");
		commandAgent = new OrderCommandAgent(repository,consumerMock,orderEventProducerMock,errorEventProducerMock,config);
		OrderEventAgent eventAgent = new OrderEventAgent(consumerMock,repository);
		liveness = new StarterLivenessCheck(commandAgent,eventAgent);
		readiness = new StarterReadinessCheck(commandAgent,eventAgent);
	}

	@Test
	public void testAppIsReady() {

		boolean ready =  readiness.isReady();
		Assertions.assertTrue(ready );
	}

	@Test
	public void testAppIsALive() {
		boolean ready =  liveness.isAlive();
		Assertions.assertTrue(ready );
	}

	/**
	 * Mock failure on communication while processing a command event
	 */
	@Test
	public void injectConnectionError() {
		orderEventProducerMock.failure=true;
		Assertions.assertTrue(liveness.isAlive());
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		OrderCommandEvent commandEvent = new OrderCommandEvent(System.currentTimeMillis(),
				"v1",
				order.toShippingOrderPayload(),
				OrderCommandEvent.ORDER_CREATED_TYPE);
		try {
			commandAgent.handleTransaction(commandEvent,null);
		} catch(Exception e) {
			// expected
		}
		Assertions.assertFalse(liveness.isAlive() );
		return ;
	}

}
