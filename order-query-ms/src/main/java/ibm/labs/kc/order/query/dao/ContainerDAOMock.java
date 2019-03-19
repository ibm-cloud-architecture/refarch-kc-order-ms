package ibm.labs.kc.order.query.dao;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerDAOMock implements ContainerDAO{
	
	private static final Logger logger = LoggerFactory.getLogger(ContainerDAO.class);

    private final Map<String, QueryContainer> containers;

    private static ContainerDAOMock instance;

    public synchronized static ContainerDAO instance() {
        if (instance == null) {
            instance = new ContainerDAOMock();
        }
        return instance;
    }

    // for testing
    public ContainerDAOMock() {
    	containers = new ConcurrentHashMap<>();
    }

    // Getting the container based on the id
	@Override
	public Optional<QueryContainer> getById(String containerId) {
		QueryContainer queryContainer = containers.get(containerId);
        return Optional.ofNullable(queryContainer);
	}

	// Storing the containers
	@Override
	public void add(QueryContainer container) {
		logger.info("Adding container id " + container.getContainerID());
        if (containers.putIfAbsent(container.getContainerID(), container) != null) {
            throw new IllegalStateException("container already exists " + container.getContainerID());
        }
	}

	// Updating the container based on the recent status
	@Override
	public void update(QueryContainer container) {
		logger.info("Updating container id " + container.getContainerID());
        if (containers.replace(container.getContainerID(), container) == null) {
            throw new IllegalStateException("container does not already exist " + container.getContainerID());
        }	
	}

}
