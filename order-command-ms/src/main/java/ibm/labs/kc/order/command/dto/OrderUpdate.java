package ibm.labs.kc.order.command.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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


    public static void validate(OrderUpdate co) {
        if (co.getOrderID() == null) {
            throw new IllegalArgumentException("Order ID is null");
        }
        OrderCreate.validate(co);
    }
}
