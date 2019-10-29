package ut;

import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ibm.gse.orderms.app.StarterLivenessCheck;
import ibm.gse.orderms.app.StarterReadinessCheck;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.command.events.OrderCommandEvent;
import ibm.gse.orderms.infrastructure.events.EventEmitter;
import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.kafka.OrderEventAgent;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepository;
import ibm.gse.orderms.infrastructure.repository.ShippingOrderRepositoryMock;


public class TestReadinessLiveness {

	static OrderEventEmitterMock orderEventProducerMock;
	static KafkaConsumerMockup consumerMock;
	
	static StarterLivenessCheck liveness;
	static StarterReadinessCheck readiness;
	static OrderCommandAgent commandAgent;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Properties properties = ShippingOrderTestDataFactory.buildConsumerKafkaProperties();
		consumerMock = new KafkaConsumerMockup<String,String>(properties,"orderCommands");	
		orderEventProducerMock = new OrderEventEmitterMock();
		ShippingOrderRepository repository = new ShippingOrderRepositoryMock();
		EventEmitter errorEventProducerMock = new OrderEventEmitterMock();
		commandAgent = new OrderCommandAgent(repository,consumerMock,orderEventProducerMock,errorEventProducerMock);
		OrderEventAgent eventAgent = new OrderEventAgent(consumerMock,repository);
		liveness = new StarterLivenessCheck(commandAgent,eventAgent);
		readiness = new StarterReadinessCheck(commandAgent,eventAgent);
	}

	@Test
	public void testAppIsReady() {
		
		boolean ready =  readiness.isReady();
		Assert.assertTrue(ready );
	}
	
	@Test
	public void testAppIsALive() {
		boolean ready =  liveness.isAlive();
		Assert.assertTrue(ready );
	}
	
	/**
	 * Mock failure on communication while processing a command event
	 */
	@Test
	public void injectConnectionError() {
		orderEventProducerMock.failure=true;
		Assert.assertTrue(liveness.isAlive());
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		OrderCommandEvent commandEvent = new OrderCommandEvent(System.currentTimeMillis(), 
				"v1", 
				order,
				OrderCommandEvent.TYPE_CREATE_ORDER);
		try {
			commandAgent.handle(commandEvent);
		} catch(Exception e) {
			// expected
		}
		Assert.assertFalse(liveness.isAlive() );
		return ;
	}

}
