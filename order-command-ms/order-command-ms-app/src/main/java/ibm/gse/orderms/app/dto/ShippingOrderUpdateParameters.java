package ibm.gse.orderms.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ibm.gse.orderms.domain.model.order.ShippingOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingOrderUpdateParameters extends ShippingOrderParameters {

    private String orderID;
    private String status;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
    	this.status = status;
    }

    public static void validate(ShippingOrderUpdateParameters co, ShippingOrder existingOrder) {
        if (co.getOrderID() == null) {
            throw new IllegalArgumentException("Order ID is null");
        }
        if (!ShippingOrder.PENDING_STATUS.equals(existingOrder.getStatus())) {
            throw new IllegalArgumentException("Order " + co.getOrderID() + " cannot be updated anymore");
        }
        ShippingOrderCreateParameters.validateInputData(co);
    }
}
