package org.tutske.rest.internals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.tutske.rest.data.RestStructure;

import java.io.Writer;
import java.util.Map;


public class JsonSerializer implements Serializer {

	private final Gson gson = new GsonBuilder ().setPrettyPrinting ().create ();

	@Override
	public String serialize (RestStructure structure, Map<String, String> attributes) {
		return gson.toJson (structure.asRestStructure ());
	}

	@Override
	public void serialize (RestStructure structure, Map<String, String> attributes, Writer writer) {
		gson.toJson (structure.asRestStructure (), writer);
	}

}
