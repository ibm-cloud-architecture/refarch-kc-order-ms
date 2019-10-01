package ibm.gse.orderms.app;

/**
 * App Registry is a one place to get access to resources of the application.
 * @author jeromeboyer
 *
 */
public class AppRegistry {

	private ShippingOrderResource orderResource = null;
	
	private static AppRegistry instance = new AppRegistry();
	
	public static AppRegistry getInstance() {
		return instance;
		
	}
	public  ShippingOrderResource orderResource() {
		synchronized(instance) {
			if (orderResource == null ) {
				orderResource = new ShippingOrderResource();
			}
		}
		return orderResource;
	}

}
