package ibm.gse.orderms.infrastructure.events;

public interface EventEmitter {

    public void emit(EventBase event) throws Exception;
    public void safeClose();

}
