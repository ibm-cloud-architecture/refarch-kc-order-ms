package ibm.labs.kc.order.command.model.events;

public interface EventEmitter {

    public void emit(Event event) throws Exception;

}
