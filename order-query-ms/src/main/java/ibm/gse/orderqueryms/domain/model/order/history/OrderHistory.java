package ibm.gse.orderqueryms.domain.model.order.history;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class OrderHistory { // implements Comparable<OrderAction>{
	
	private OrderHistoryInfo orderActionItem;
	private long timestampMillis;
	private String action;
	private String type;
	
	public OrderHistory(long timestampMillis, String action, String type){
		this.timestampMillis = timestampMillis;
		this.action = action;
		this.type = type;
	}
	
	public OrderHistory(OrderHistoryInfo orderActionItem,long timestampMillis, String action, String type){
		this.setOrderActionItem(orderActionItem);
		this.timestampMillis = timestampMillis;
		this.action = action;
		this.type = type;
	}
	
	public static OrderHistory newFromHistoryOrder(OrderHistoryInfo orderActionItem, long timestampMillis, String action) {
		return new OrderHistory(timestampMillis, action, "order");
    }
	
	public static OrderHistory newFromOrder(OrderHistoryInfo orderActionItem, long timestampMillis, String action) {
		return new OrderHistory(orderActionItem, timestampMillis, action, "order");
    }
	
	public static OrderHistory newFromHistoryContainer(OrderHistoryInfo orderActionItem, long timestampMillis, String action) {
		return new OrderHistory(timestampMillis, action, "container");
    }
	
	public static OrderHistory newFromContainer(OrderHistoryInfo orderActionItem, long timestampMillis, String action) {
		return new OrderHistory(orderActionItem, timestampMillis, action, "container");
    }
	
	public OrderHistoryInfo getOrderActionItem() {
		return orderActionItem;
	}

	public void setOrderActionItem(OrderHistoryInfo orderActionItem) {
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

//	@Override
//	public int compareTo(OrderAction o) {
//		return Long.compare(this.getTimestampMillis(), o.getTimestampMillis()); 
//	}

}
