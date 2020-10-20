package ibm.gse.orderms.domain.events;

public interface EventEmitter {

    public void emit(EventBase event) throws Exception;
    public void safeClose();

}
