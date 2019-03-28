package ibm.labs.kc.order.query.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Container {
	
	public static final String ADDED_STATUS = "ContainerAdded";
	public static final String REMOVED_STATUS = "ContainerRemoved";
	public static final String AT_LOCATION = "ContainerAtLocation";
	public static final String PICK_UP_SITE_STATUS = "atPickUpSite";
	public static final String DOOR_OPEN_STATUS = "doorOpen";
	public static final String GOODS_LOADED_STATUS = "goodsLoaded";
	public static final String DOOR_CLOSED_STATUS = "doorClosed";
	public static final String AT_DOCK_STATUS = "atDock";
	
	private String containerID;
	private String brand;
	private String type;
	private int capacity;
	private double latitude;
	private double longitude;
	private String status;
	
	public Container(String containerID, String brand, String type, int capacity, double latitude, double longitude, String status){
		this.containerID = containerID;
		this.brand = brand;
		this.type = type;
		this.capacity = capacity;
		this.latitude = latitude;
		this.longitude = longitude;
		this.status = status;
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
