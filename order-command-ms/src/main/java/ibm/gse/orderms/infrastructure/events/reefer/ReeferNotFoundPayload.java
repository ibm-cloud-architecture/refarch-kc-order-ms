package ibm.gse.orderms.infrastructure.events.reefer;

public class ReeferNotFoundPayload {
	 private String orderID;
	 private String reason;
	 
	 public ReeferNotFoundPayload(String oid, String reason) {
		 this.orderID = oid;
		 this.reason = reason;
	 }

	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
