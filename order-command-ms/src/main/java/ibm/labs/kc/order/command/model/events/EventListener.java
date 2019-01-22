package ibm.labs.kc.order.command.model.events;

public interface EventListener {

    public void handle(Event event);

}
