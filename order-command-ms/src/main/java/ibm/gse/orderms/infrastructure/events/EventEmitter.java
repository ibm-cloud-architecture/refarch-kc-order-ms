package ibm.gse.orderms.infrastructure.events;

public interface EventEmitter {

    public void emit(OrderEventAbstract event) throws Exception;
    public void safeClose();

}
