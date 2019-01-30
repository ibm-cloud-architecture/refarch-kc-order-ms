package ibm.labs.kc.order.query.model;

public class QueryOrder {

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
    private String reason;

    public QueryOrder(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
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

    public static QueryOrder newFromOrder(Order order) {
        return new QueryOrder(order.getOrderID(),
                order.getProductID(), order.getCustomerID(), order.getQuantity(),
                order.getPickupAddress(), order.getPickupDate(),
                order.getDestinationAddress(), order.getExpectedDeliveryDate(),
                order.getStatus());
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
    }

    public void assign(VoyageAssignment voyageAssignment) {
        this.voyageID = voyageAssignment.getVoyageID();
        this.status = Order.ASSIGNED_STATUS;
    }

    public void cancel(Cancellation cancellation) {
        this.status = Order.CANCELLED_STATUS;
        this.reason = cancellation.getReason();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVoyageID() {
        return voyageID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public Address getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(Address destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(String expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((voyageID == null) ? 0 : voyageID.hashCode());
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
        QueryOrder other = (QueryOrder) obj;
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
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (voyageID == null) {
            if (other.voyageID != null)
                return false;
        } else if (!voyageID.equals(other.voyageID))
            return false;
        return true;
    }

}
