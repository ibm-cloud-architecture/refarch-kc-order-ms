package ibm.labs.kc.order.command.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ibm.labs.kc.order.command.dao.CommandOrder;
import ibm.labs.kc.order.command.model.Order;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderUpdate extends OrderRequest {

    private String orderID;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    //TODO need to ignore this
    public String getStatus() {
        return "";
    }

    public void setStatus(String status) {
    }

    public static void validate(OrderUpdate co, CommandOrder existingOrder) {
        if (co.getOrderID() == null) {
            throw new IllegalArgumentException("Order ID is null");
        }
        if (!Order.PENDING_STATUS.equals(existingOrder.getStatus())) {
            throw new IllegalArgumentException("Order " + co.getOrderID() + " cannot be updated anymore");
        }
        OrderCreate.validate(co);
    }
}
