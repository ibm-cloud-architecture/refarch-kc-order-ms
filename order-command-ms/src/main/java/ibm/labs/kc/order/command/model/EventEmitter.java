package ibm.labs.kc.order.command.model;

public interface EventEmitter {

    public void emit(Event event) throws Exception;

}
