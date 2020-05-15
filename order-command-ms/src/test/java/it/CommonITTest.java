package it;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

public abstract class CommonITTest {
	 protected static String port = (System.getProperty("liberty.test.port") != null) ? System.getProperty("liberty.test.port"):"9080";
	 protected String endpoint = "/orders";
	 protected String url = "http://localhost:" + port + endpoint;

	 protected Response makePutRequest(String url, String json) {
	        Client client = ClientBuilder.newClient();
	        Invocation.Builder invoBuild = client.target(url).request();
	        Response response = invoBuild.put(Entity.json(json));
	        return response;
	    }

	    protected Response makeGetRequest(String url) {
	        Client client = ClientBuilder.newClient();
	        Invocation.Builder invoBuild = client.target(url).request();
	        Response response = invoBuild.get();
	        return response;
	    }

	    protected Response makePostRequest(String url, String json) {
	        Client client = ClientBuilder.newClient();
	        Invocation.Builder invoBuild = client.target(url).request();
	        Response response = invoBuild.post(Entity.json(json));
	        return response;
	    }
}
