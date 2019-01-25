package ibm.labs.kc.order.command.model;

public class Order {

    public static final String PENDING_STATUS = "pending";
    public static final String CANCELLED_STATUS = "cancelled";
    public static final String ASSIGNED_STATUS = "assigned";

    private String orderID;
    private String productID;
    private String customerID;
    private int quantity;

    private Address pickupAddress;
    private String pickupDate;

    private Address destinationAddress;
    private String expectedDeliveryDate;

    private String status;

    public Order() {
    }

    public Order(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
            String pickupDate, Address destinationAddress, String expectedDeliveryDate, String status) {
        super();
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

    public String getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public Address getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(Address destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((customerID == null) ? 0 : customerID.hashCode());
        result = prime * result + ((destinationAddress == null) ? 0 : destinationAddress.hashCode());
        result = prime * result + ((expectedDeliveryDate == null) ? 0 : expectedDeliveryDate.hashCode());
        result = prime * result + ((orderID == null) ? 0 : orderID.hashCode());
        result = prime * result + ((pickupAddress == null) ? 0 : pickupAddress.hashCode());
        result = prime * result + ((pickupDate == null) ? 0 : pickupDate.hashCode());
        result = prime * result + ((productID == null) ? 0 : productID.hashCode());
        result = prime * result + quantity;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Order other = (Order) obj;
        if (customerID == null) {
            if (other.customerID != null)
                return false;
        } else if (!customerID.equals(other.customerID))
            return false;
        if (destinationAddress == null) {
            if (other.destinationAddress != null)
                return false;
        } else if (!destinationAddress.equals(other.destinationAddress))
            return false;
        if (expectedDeliveryDate == null) {
            if (other.expectedDeliveryDate != null)
                return false;
        } else if (!expectedDeliveryDate.equals(other.expectedDeliveryDate))
            return false;
        if (orderID == null) {
            if (other.orderID != null)
                return false;
        } else if (!orderID.equals(other.orderID))
            return false;
        if (pickupAddress == null) {
            if (other.pickupAddress != null)
                return false;
        } else if (!pickupAddress.equals(other.pickupAddress))
            return false;
        if (pickupDate == null) {
            if (other.pickupDate != null)
                return false;
        } else if (!pickupDate.equals(other.pickupDate))
            return false;
        if (productID == null) {
            if (other.productID != null)
                return false;
        } else if (!productID.equals(other.productID))
            return false;
        if (quantity != other.quantity)
            return false;
        return true;
    }

    public void setExpectedDeliveryDate(String expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public Address getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(Address pickupAddress) {
        this.pickupAddress = pickupAddress;
    }
    
    public String getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
