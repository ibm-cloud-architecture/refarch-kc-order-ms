package ut.orderms.domain;

import java.lang.reflect.Type;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;


import ibm.gse.orderms.infrastructure.events.OrderEvent;

public class TestSerialization {

	class EventStructure {
		public String type;
		public JsonObject payload;
		public EventStructure(String t, JsonObject p) {
			this.type = t;
			this.payload = p;
		}
	}
	
	class EventPayload1 {
		String oid;
		String cid;
		
		public EventPayload1(String oid, String cid) {
			this.cid = cid;
			this.oid = oid;
		}
	}
	
	class EventPayload2 {
		String oid;
		String vid;
		String pid;
		
		public EventPayload2(String oid, String vid,String pid) {
			this.vid = vid;
			this.oid = oid;
			this.pid = pid;
		}
	}
	
	/**
	 * parse depending of the type
	 */
	@Test
	public void test() {
		Gson gson = new Gson();
		EventPayload1 p1 = new EventPayload1("o01","c01");
		JsonParser parser = new JsonParser();
		String valueP1 = gson.toJson(p1);
		System.out.println(valueP1);
		
		EventPayload2 p2 = new EventPayload2("o01","v01","p01");
		String valueP2 = gson.toJson(p2);
		System.out.println(valueP2);
		
		JsonElement element = gson.fromJson (valueP2, JsonElement.class);
		JsonObject jsonObj = element.getAsJsonObject();
		
		
	
		
		EventStructure oevent = new EventStructure(OrderEvent.TYPE_ORDER_CREATED,jsonObj);
		String value = new Gson().toJson(oevent);
		System.out.println(value);
		EventStructure oeventOut = gson.fromJson(value, EventStructure.class);
		
		System.out.println(oeventOut.payload);
	}

}
