package ibm.labs.kc.order.query.dao;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

import ibm.labs.kc.order.query.model.Container;


public class ContainerDAOMockTest {
	
	@Test
    public void testGetById() {
		ContainerDAOMock dao = new ContainerDAOMock();
        QueryContainer c1 = QueryContainer.newFromContainer(new Container("containerID", "brand", "type", 1, 1, 1, Container.AVAILABLE_STATUS));
        QueryContainer c2 = QueryContainer.newFromContainer(new Container(UUID.randomUUID().toString(), "brand", "type", 1, 1, 1, Container.AVAILABLE_STATUS));

        dao.add(c1);
        dao.add(c2);

        Optional<QueryContainer> byId = dao.getById("containerID");
        assertEquals(c1, byId.orElse(null));
    }

}
