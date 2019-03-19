package ibm.labs.kc.order.query.dao;

import java.util.Optional;

public interface ContainerDAO {
	
	public Optional<QueryContainer> getById(String containerId);
	public void add(QueryContainer container);
    public void update(QueryContainer container);

}
