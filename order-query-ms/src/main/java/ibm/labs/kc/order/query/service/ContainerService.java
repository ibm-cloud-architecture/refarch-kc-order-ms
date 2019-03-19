package ibm.labs.kc.order.query.service;

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ibm.labs.kc.order.query.dao.ContainerDAO;
import ibm.labs.kc.order.query.dao.ContainerDAOMock;
import ibm.labs.kc.order.query.dao.QueryContainer;
import ibm.labs.kc.order.query.model.Container;
import ibm.labs.kc.order.query.model.ContainerAssignment;
import ibm.labs.kc.order.query.model.events.ContainerAllocationEvent;
import ibm.labs.kc.order.query.model.events.ContainerEvent;
import ibm.labs.kc.order.query.model.events.CreateContainerEvent;
import ibm.labs.kc.order.query.model.events.Event;
import ibm.labs.kc.order.query.model.events.EventListener;

@Path("containers")
public class ContainerService implements EventListener {
	
	static final Logger logger = LoggerFactory.getLogger(ContainerService.class);

    private ContainerDAO containerDAO;

    public ContainerService() {
    	containerDAO = ContainerDAOMock.instance();
    }
    
    @GET
    @Path("{Id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Query a container by id", description = "")
    @APIResponses(value = {
            @APIResponse(responseCode = "404", description = "Container not found", content = @Content(mediaType = "text/plain")),
            @APIResponse(responseCode = "200", description = "Container found", content = @Content(mediaType = "application/json")) })
    public Response getById(@PathParam("Id") String containerId) {
        logger.info("ContainerService.getById(" + containerId + ")");

        Optional<QueryContainer> queryContainer = containerDAO.getById(containerId);
        if (queryContainer.isPresent()) {
            return Response.ok().entity(queryContainer.get()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

	@Override
	public void handle(Event event) {
		String containerID;
        Optional<QueryContainer> qc;
        try {
            ContainerEvent containerEvent = (ContainerEvent) event;
            if(containerEvent!=null){
            	System.out.println("@@@@ in handle container" + new Gson().toJson(containerEvent));
                switch (containerEvent.getType()) {
                case ContainerEvent.TYPE_CREATED:
                    synchronized (containerDAO) {
                        Container conatiner = ((CreateContainerEvent) containerEvent).getPayload();
                        QueryContainer queryContainer = QueryContainer.newFromContainer(conatiner);
                        containerDAO.add(queryContainer);
                    }
                    break;
                case ContainerEvent.TYPE_ASSIGNED:
                    synchronized (containerDAO) {
                    	ContainerAssignment container = ((ContainerAllocationEvent) containerEvent).getPayload();
                        containerID = container.getContainerID();
                        qc = containerDAO.getById(containerID);
                        if (qc.isPresent()) {
                            QueryContainer queyContainer = qc.get();
                            queyContainer.assignedToOrder(container);
                            containerDAO.update(queyContainer);
                        } else {
                            throw new IllegalStateException("Cannot update - Unknown order Id " + containerID);
                        }
                    }
                    break;
                default:
                    logger.warn("Unknown event type: " + containerEvent);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
	}

}
