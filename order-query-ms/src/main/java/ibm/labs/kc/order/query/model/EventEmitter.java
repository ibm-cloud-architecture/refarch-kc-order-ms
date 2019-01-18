package ibm.labs.kc.order.query.model;

public interface EventEmitter {

    public void emit(Event event) throws Exception;

}
