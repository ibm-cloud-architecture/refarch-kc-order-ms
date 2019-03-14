package ibm.labs.kc.order.query.action;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class OrderAction {
	
	private OrderActionInfo orderActionItem;
	private long timestampMillis;
	private String action;
	private String type;
	
	public OrderAction(long timestampMillis, String action, String type){
		this.timestampMillis = timestampMillis;
		this.action = action;
		this.type = type;
	}
	
	public OrderAction(OrderActionInfo orderActionItem,long timestampMillis, String action, String type){
		this.setOrderActionItem(orderActionItem);
		this.timestampMillis = timestampMillis;
		this.action = action;
		this.type = type;
	}
	
	public static OrderAction newFromHistoryOrder(OrderActionInfo orderActionItem, long timestampMillis, String action) {
		return new OrderAction(timestampMillis, action, "order");
    }
	
	public static OrderAction newFromOrder(OrderActionInfo orderActionItem, long timestampMillis, String action) {
		return new OrderAction(orderActionItem, timestampMillis, action, "order");
    }
	
	public OrderActionInfo getOrderActionItem() {
		return orderActionItem;
	}

	public void setOrderActionItem(OrderActionInfo orderActionItem) {
		this.orderActionItem = orderActionItem;
	}
	
	public long getTimestampMillis() {
		return timestampMillis;
	}
	
	public void setTimestampMillis(long timestampMillis) {
		this.timestampMillis = timestampMillis;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
