package ibm.labs.kc.order.query.model.events;

public interface EventEmitter {

    public void emit(Event event) throws Exception;

}
