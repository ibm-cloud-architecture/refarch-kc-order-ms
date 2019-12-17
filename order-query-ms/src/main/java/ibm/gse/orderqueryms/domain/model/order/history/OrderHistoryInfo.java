package ibm.gse.orderqueryms.domain.model.order.history;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ibm.gse.orderqueryms.domain.model.Address;
import ibm.gse.orderqueryms.domain.model.Cancellation;
import ibm.gse.orderqueryms.domain.model.Container;
import ibm.gse.orderqueryms.domain.model.ContainerAssignment;
import ibm.gse.orderqueryms.domain.model.Order;
import ibm.gse.orderqueryms.domain.model.Rejection;
import ibm.gse.orderqueryms.domain.model.Spoil;
import ibm.gse.orderqueryms.domain.model.VoyageAssignment;

public class OrderHistoryInfo {
	
	private String orderID;
    private String productID;
    private String customerID;
    private int quantity;
    private Address pickupAddress;
    private String pickupDate;
    private Address destinationAddress;
    private String expectedDeliveryDate;
    private String status;
    private String voyageID;
    private String containerID;
    private String reason;
	private String brand;
	private String type;
	private int capacity;
	private double latitude;
	private double longitude;

    public OrderHistoryInfo(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
            String pickupDate, Address destinationAddress, String expectedDeliveryDate, String status) {
        this.orderID = orderID;
        this.productID = productID;
        this.customerID = customerID;
        this.quantity = quantity;
        this.pickupAddress = pickupAddress;
        this.pickupDate = pickupDate;
        this.destinationAddress = destinationAddress;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
    }
    
    public OrderHistoryInfo(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
            String pickupDate, Address destinationAddress, String expectedDeliveryDate, String status, String voyageID) {
        this.orderID = orderID;
        this.productID = productID;
        this.customerID = customerID;
        this.quantity = quantity;
        this.pickupAddress = pickupAddress;
        this.pickupDate = pickupDate;
        this.destinationAddress = destinationAddress;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
        this.voyageID = voyageID;
    }
    
    public OrderHistoryInfo(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
            String pickupDate, Address destinationAddress, String expectedDeliveryDate, String status, String voyageID, String containerID) {
        this.orderID = orderID;
        this.productID = productID;
        this.customerID = customerID;
        this.quantity = quantity;
        this.pickupAddress = pickupAddress;
        this.pickupDate = pickupDate;
        this.destinationAddress = destinationAddress;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
        this.voyageID = voyageID;
        this.containerID = containerID;
    }
    
    public OrderHistoryInfo(String containerID, String brand, String type, int capacity, double latitude, double longitude, String status) {
    	this.containerID = containerID;
		this.setBrand(brand);
		this.setType(type);
		this.setCapacity(capacity);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.status = status;
    }

    public static OrderHistoryInfo newFromOrder(Order order) {
        return new OrderHistoryInfo(order.getOrderID(),
                order.getProductID(), order.getCustomerID(), order.getQuantity(),
                order.getPickupAddress(), order.getPickupDate(),
                order.getDestinationAddress(), order.getExpectedDeliveryDate(),
                order.getStatus());
    }
    
    public static OrderHistoryInfo newFromContainer(Container container) {
        return new OrderHistoryInfo(container.getContainerID(),
                container.getBrand(), container.getType(), container.getCapacity(),
                container.getLatitude(), container.getLongitude(),
                container.getStatus());
    }

    public void update(Order order) {
        if (!Order.PENDING_STATUS.contentEquals(status)) {
            throw new IllegalStateException(
                    "Unable to update a QueryOrder not in " + Order.PENDING_STATUS + " state");
        }
        if (order.getCustomerID() != null) {
            customerID = order.getCustomerID();
        }
        if (order.getProductID() != null) {
            productID = order.getProductID();
        }
        if (order.getQuantity() != 0) {
            quantity = order.getQuantity();
        }
        if (order.getPickupAddress() != null) {
            pickupAddress = order.getPickupAddress();
        }
        if (order.getPickupDate() != null) {
            pickupDate = order.getPickupDate();
        }
        if (order.getDestinationAddress() != null) {
            destinationAddress = order.getDestinationAddress();
        }
        if (order.getExpectedDeliveryDate() != null) {
            expectedDeliveryDate = order.getExpectedDeliveryDate();
        }
        if (order.getVoyageID() != null) {
            voyageID = order.getVoyageID();
        }
        if (order.getContainerID() != null) {
        	containerID = order.getContainerID();
        }
    }

    public void assign(VoyageAssignment voyageAssignment) {
        this.voyageID = voyageAssignment.getVoyageID();
        this.status = Order.ASSIGNED_STATUS;
    }

    public void assignContainer(ContainerAssignment ca) {
    	this.containerID = ca.getContainerID();
    	this.status = Order.CONTAINER_ALLOCATED_STATUS;
    }
    
    public void cancel(Cancellation cancellation) {
        this.status = Order.CANCELLED_STATUS;
        this.reason = cancellation.getReason();
    }
    
    public void reject(Rejection rejection){
        this.status = Order.REJECTED_STATUS;
        this.reason = rejection.getReason();
    }
    
    public void containerRemoved(Container container){
    	this.status = Container.REMOVED_STATUS;
    }
    
    public void containerAtLocation(Container container){
    	this.status = Container.AT_LOCATION_STATUS;
    }
    
    public void containerOnMaintenance(Container container){
    	this.status = Container.ON_MAINTENANCE_STATUS;
    }
    
    public void containerOffMaintenance(Container container){
    	this.status = Container.OFF_MAINTENANCE_STATUS;
    }
    
    public void containerOrderAssignment(Container container){
    	this.status = Container.ORDER_ASSIGNED_STATUS;
    }
    
    public void containerOrderReleased(Container container){
    	this.status = Container.ORDER_RELEASED_STATUS;
    }
    
    public void containerGoodsLoaded(Container container){
    	this.status = Container.GOODS_LOADED_STATUS;
    }
    
    public void containerGoodsUnloaded(Container container){
    	this.status = Container.GOOD_UNLOADED_STATUS;
    }
    
    public void containerOnShip(Container container){
    	this.status = Container.ON_SHIP_STATUS;
    }
    
    public void containerOffShip(Container container){
    	this.status = Container.OFF_SHIP_STATUS;
    }
    
    public void containerOnTruck(Container container){
    	this.status = Container.ON_TRUCK_STATUS;
    }
    
    public void containerOffTruck(Container container){
    	this.status = Container.OFF_TRUCK_STATUS;
    }
    
    public void containerDelivered(ContainerAssignment container){
    	this.status = Order.CONTAINER_DELIVERED_STATUS;
    }
    
    public void orderCompleted(Order order){
    	this.status = Order.ORDER_COMPLETED_STATUS;
    }

    public void spoil(Spoil spoil){
    	this.status = Order.SPOILT_STATUS;
    }

    public String getStatus() {
        return status;
    }

    public String getVoyageID() {
        return voyageID;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getProductID() {
        return productID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public int getQuantity() {
        return quantity;
    }

    public Address getPickupAddress() {
        return pickupAddress;
    }

    public String getPickupDate() {
        return pickupDate;
    }

    public Address getDestinationAddress() {
        return destinationAddress;
    }

    public String getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}

	public void setVoyageID(String voyageID) {
		this.voyageID = voyageID;
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

}
