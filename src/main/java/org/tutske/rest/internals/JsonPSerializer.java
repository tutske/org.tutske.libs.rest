package org.tutske.rest.internals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.tutske.rest.data.RestStructure;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;


public class JsonPSerializer implements Serializer {

	private final static String CALLBACK_KEY = "callback";
	private final static Gson gson = new GsonBuilder ().setPrettyPrinting ().create ();

	@Override
	public String serialize (RestStructure structure, Map<String, String> attributes) {
		assureCallback (attributes);
		return attributes.get (CALLBACK_KEY) + " (" +
			gson.toJson (structure.asRestStructure ()) +
		");";
	}

	@Override
	public void serialize (RestStructure structure, Map<String, String> attributes, Writer writer) {
		assureCallback (attributes);
		try {
			writer.write (attributes.get (CALLBACK_KEY));
			writer.write (" (");
			gson.toJson (structure.asRestStructure (), writer);
			writer.write (");");
		} catch (IOException exception) {
			throw new RuntimeException (exception);
		}
	}

	private void assureCallback (Map<String, String> attributes) {
		if ( ! attributes.containsKey (CALLBACK_KEY) ) {
			throw new RuntimeException ("No callback specified for jsonp serialization");
		}
	}

}
