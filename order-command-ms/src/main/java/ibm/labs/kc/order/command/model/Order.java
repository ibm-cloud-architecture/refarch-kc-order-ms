package ibm.labs.kc.order.command.model;

public class Order {
    
    private String orderID;
    private String productID;
    private String customerID;
    private int quantity;

    private Address pickupAddress;
    private String pickupDate;

    private Address destinationAddress;
    private String expectedDeliveryDate;

    public Order() {
    }

    public Order(String orderID, String productID, String customerID, int quantity, Address pickupAddress,
            String pickupDate, Address destinationAddress, String expectedDeliveryDate) {
        super();
        this.orderID = orderID;
        this.productID = productID;
        this.customerID = customerID;
        this.quantity = quantity;
        this.pickupAddress = pickupAddress;
        this.pickupDate = pickupDate;
        this.destinationAddress = destinationAddress;
        this.expectedDeliveryDate = expectedDeliveryDate;
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
}
