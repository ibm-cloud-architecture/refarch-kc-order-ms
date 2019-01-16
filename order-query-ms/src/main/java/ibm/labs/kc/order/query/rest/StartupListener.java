package ibm.labs.kc.order.query.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.servlet.ServletContext;

import ibm.labs.kc.order.query.dao.OrderDAO;
import ibm.labs.kc.order.query.dao.OrderDAOMock;

@ApplicationScoped
public class StartupListener {

    private OrderDAO orderDAO;

    public void init(@Observes 
                     @Initialized(ApplicationScoped.class) ServletContext context) {
        // Perform action during application's startup
        orderDAO = OrderDAOMock.instance();
    }

    public void destroy(@Observes 
                        @Destroyed(ApplicationScoped.class) ServletContext context) {
        // Perform action during application's shutdown
    }
}