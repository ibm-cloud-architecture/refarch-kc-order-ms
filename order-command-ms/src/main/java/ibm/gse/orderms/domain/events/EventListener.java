package ibm.gse.orderms.domain.events;

public interface EventListener {

    public void handle(EventBase event);

}
