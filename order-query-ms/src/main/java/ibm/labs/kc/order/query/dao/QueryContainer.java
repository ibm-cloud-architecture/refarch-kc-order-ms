package ibm.labs.kc.order.query.dao;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ibm.labs.kc.order.query.model.Container;
import ibm.labs.kc.order.query.model.ContainerAssignment;

public class QueryContainer {
	
	private String containerID;
	private String brand;
	private String type;
	private int capacity;
	private double latitude;
	private double longitude;
	private String status;
	
	public QueryContainer(String containerID, String brand, String type, int capacity, double latitude, double longitude, String status){
		this.containerID = containerID;
		this.brand = brand;
		this.type = type;
		this.capacity = capacity;
		this.latitude = latitude;
		this.longitude = longitude;
		this.status = status;
	}
	
	public static QueryContainer newFromContainer(Container container) {
        return new QueryContainer(container.getContainerID(), container.getBrand(), container.getType(), 
        		container.getCapacity(), container.getLatitude(), container.getLongitude(), container.getStatus());
    }
	
	public void update(Container container){
		if (!Container.AVAILABLE_STATUS.contentEquals(status)) {
            throw new IllegalStateException(
                    "Unable to update a QueryContainer not in " + Container.AVAILABLE_STATUS + " state");
        }
        if (container.getBrand() != null) {
        	brand = container.getBrand();
        }
        if (container.getType() != null) {
        	type = container.getType();
        }
        if (container.getCapacity() != 0) {
        	capacity = container.getCapacity();
        }
        if (container.getLatitude() != 0) {
        	latitude = container.getLatitude();
        }
        if (container.getLongitude() != 0) {
        	longitude = container.getLongitude();
        }
        if (container.getStatus() != null) {
        	status = container.getStatus();
        }
		
	}
	
	public void assignedToOrder(ContainerAssignment ca) {
    	this.status = Container.ALLOCATED_TO_ORDER_STATUS;
    }

	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
