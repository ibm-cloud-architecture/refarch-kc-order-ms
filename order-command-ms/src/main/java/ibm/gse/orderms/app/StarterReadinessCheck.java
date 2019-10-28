package ibm.gse.orderms.app;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import ibm.gse.orderms.infrastructure.kafka.OrderCommandAgent;
import ibm.gse.orderms.infrastructure.kafka.OrderEventAgent;

@Readiness
@ApplicationScoped
public class StarterReadinessCheck implements HealthCheck {

	@Inject
	OrderCommandAgent orderCommandsAgent;
	
	@Inject
	OrderEventAgent orderEventAgent;
	
	public StarterReadinessCheck(OrderCommandAgent commandAgent, OrderEventAgent eventAgent) {
		this.orderCommandsAgent = commandAgent;
		this.orderEventAgent = eventAgent;
	}

	/**
	 * Validate kafka consumers are ready
	 * @return
	 */
    public boolean isReady() {
        // perform readiness checks, e.g. database connection, etc.
    	boolean status = orderCommandsAgent.isRunning() && orderEventAgent.isRunning();
        
        return status;
    }
	
    @Override
    public HealthCheckResponse call() {
        boolean up = isReady();
        return HealthCheckResponse.named(this.getClass().getSimpleName()).state(up).build();
    }
    
}
