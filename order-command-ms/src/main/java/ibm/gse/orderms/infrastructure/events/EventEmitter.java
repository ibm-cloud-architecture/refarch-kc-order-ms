package ibm.gse.orderms.infrastructure.events;

public interface EventEmitter {

    public void emit(OrderEventBase event) throws Exception;
    public void safeClose();

}
