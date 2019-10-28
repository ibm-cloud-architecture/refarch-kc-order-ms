package ibm.gse.orderms.app;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.kafka.OrderEventAgent;

@Liveness
@ApplicationScoped
public class StarterLivenessCheck implements HealthCheck {

	@Inject
	OrderCommandAgent orderCommandsAgent;
	
	@Inject
	OrderEventAgent orderEventAgent;
	
	public StarterLivenessCheck(OrderCommandAgent orderCommandsAgent,
			OrderEventAgent orderEventAgent) {
		this.orderCommandsAgent = orderCommandsAgent;
		this.orderEventAgent = orderEventAgent;
	}
	
	/**
	 * Verify each agent is alive
	 * @return
	 */
    public boolean isAlive() {
    	boolean status = orderCommandsAgent.isRunning() && orderEventAgent.isRunning();
        return status;
    }
	
    @Override
    public HealthCheckResponse call() {
        boolean up = isAlive();
        return HealthCheckResponse.named(this.getClass().getSimpleName()).state(up).build();
    }
    
}
