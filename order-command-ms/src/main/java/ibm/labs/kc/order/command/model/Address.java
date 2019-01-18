package ibm.labs.kc.order.command.model;

public class Address {
	private String street;
	private String city;
	private String country;
	private String state;
	private String zipcode;
	
	public Address(String street, String city, String country, String state, String zipcode) {
		super();
		this.street = street;
		this.city = city;
		this.country = country;
		this.state = state;
		this.zipcode = zipcode;
	}

	public Address() {}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	
}
