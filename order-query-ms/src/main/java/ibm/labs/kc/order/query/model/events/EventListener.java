package ibm.labs.kc.order.query.model.events;

public interface EventListener {

    public void handle(Event event);

}
