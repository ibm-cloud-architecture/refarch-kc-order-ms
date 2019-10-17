package ibm.gse.orderqueryms.infrastructure.events;

public interface EventListener {

    public void handle(AbstractEvent event);

}
