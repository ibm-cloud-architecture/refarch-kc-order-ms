package ut;

import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import ibm.gse.orderms.app.dto.ShippingOrderCreateParameters;
import ibm.gse.orderms.app.dto.ShippingOrderUpdateParameters;
import ibm.gse.orderms.domain.model.order.Address;
import ibm.gse.orderms.domain.model.order.ShippingOrder;
import ibm.gse.orderms.infrastructure.events.ShippingOrderPayload;

/**
 * A factory to generate test fixtures
 * 
 * @author jerome boyer
 *
 */
public class ShippingOrderTestDataFactory {

	
	public static ShippingOrder orderFixtureWithoutIdentity() {
		
		Address pickupAddress = new Address("Street", "City", "OriginCountry", "State", "Zipcode");
		Address destinationAddress = new Address("Street", "City", "DestinationCountry", "State", "Zipcode");
		ShippingOrder order = new ShippingOrder("","P01","AFarmer",100, pickupAddress,
				"2019-01-14T17:48Z", 
				destinationAddress, "2019-01-15T17:48Z",  ShippingOrder.PENDING_STATUS);
		return order;
	}
	
	public static ShippingOrderCreateParameters orderCreateFixtureWithoutID() {
		ShippingOrderCreateParameters dto = new ShippingOrderCreateParameters();
		dto.setCustomerID("AtestCustomer");
		dto.setProductID("P03");
		Address mockAddress = new Address("Street", "City", "Country", "State", "Zipcode");
		dto.setExpectedDeliveryDate("2019-01-15T17:48Z");
		dto.setPickupDate("2019-01-14T17:48Z");
		dto.setQuantity(100);
		dto.setCustomerID("customerID");
		dto.setDestinationAddress(mockAddress);
		dto.setPickupAddress(mockAddress);
		return dto;
	}
	
	public static ShippingOrder orderFixtureWithIdentity() {
		ShippingOrder order = 	orderFixtureWithoutIdentity();
		order.setOrderID(UUID.randomUUID().toString());
		return order;
	}
	
	
	public static ShippingOrderUpdateParameters updateOrderFixtureFromOrder(ShippingOrder existingOrder) {
		ShippingOrderUpdateParameters updateParameters = new ShippingOrderUpdateParameters();
		updateParameters.setOrderID(existingOrder.getOrderID());
		updateParameters.setProductID(existingOrder.getProductID());
		updateParameters.setCustomerID(existingOrder.getCustomerID());
		updateParameters.setStatus(existingOrder.getStatus());
		updateParameters.setQuantity(existingOrder.getQuantity());
		updateParameters.setPickupAddress(existingOrder.getPickupAddress());
		updateParameters.setPickupDate(existingOrder.getPickupDate());
		updateParameters.setDestinationAddress(existingOrder.getDestinationAddress());
		updateParameters.setExpectedDeliveryDate(existingOrder.getExpectedDeliveryDate());
		return updateParameters;
	}

	public static ShippingOrderPayload orderPayloadFixture() {
		ShippingOrder order = ShippingOrderTestDataFactory.orderFixtureWithIdentity();
		ShippingOrderPayload payload = new ShippingOrderPayload(order.getOrderID(), 
				order.getProductID(),
				order.getCustomerID(),
				order.getQuantity(), 
				order.getPickupAddress(),
				order.getPickupDate(),
				order.getDestinationAddress(),
				order.getExpectedDeliveryDate(), 
				order.getStatus());
		return payload;
	}
	
	public static Properties buildConsumerKafkaProperties() {
		Properties properties = new Properties();
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		properties.put(ConsumerConfig.GROUP_ID_CONFIG,  "test-grp");
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.toString(false));
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "test-clientID");
	
		return properties;

	}
}
