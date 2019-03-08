package ibm.labs.kc.order.query.complex;

import org.apache.commons.lang3.builder.EqualsBuilder;

import ibm.labs.kc.order.query.dao.QueryOrder;

public class ComplexQueryOrder {
	
	private String orderID;
	private long timestampMillis;
	private String action;
	private String type;
	
	public ComplexQueryOrder(long timestampMillis, String action, String type){
		this.timestampMillis = timestampMillis;
		this.action = action;
		this.type = type;
	}
	
	public ComplexQueryOrder(String orderID,long timestampMillis, String action, String type){
		this.orderID = orderID;
		this.timestampMillis = timestampMillis;
		this.action = action;
		this.type = type;
	}
	
	public static ComplexQueryOrder newFromHistoryOrder(QueryOrder queryOrder, long timestampMillis, String action) {
		return new ComplexQueryOrder(timestampMillis, action, "order");
    }
	
	public static ComplexQueryOrder newFromOrder(QueryOrder queryOrder, long timestampMillis, String action) {
		return new ComplexQueryOrder(queryOrder.getOrderID(), timestampMillis, action, "order");
    }
	
	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
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
