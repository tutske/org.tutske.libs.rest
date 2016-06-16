package org.tutske.rest.internals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.tutske.rest.data.RestStructure;

import java.io.Writer;


public class JsonSerailizer implements Serializer {

	private final Gson gson = new GsonBuilder ().setPrettyPrinting ().create ();

	@Override
	public String serialize (RestStructure structure) {
		return gson.toJson (structure.asJson ());
	}

	@Override
	public void serialize (RestStructure structure, Writer writer) {
		gson.toJson (structure.asJson (), writer);
	}

}
