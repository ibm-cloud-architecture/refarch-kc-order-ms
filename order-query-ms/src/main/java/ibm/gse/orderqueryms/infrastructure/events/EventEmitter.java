package ibm.gse.orderqueryms.infrastructure.events;

public interface EventEmitter {

    public void emit(Event event) throws Exception;

    public void safeClose();

}
