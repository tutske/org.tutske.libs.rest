package org.tutske.rest.internals;

import org.tutske.rest.data.RestStructure;

import java.io.Writer;
import java.util.Collections;
import java.util.Map;


public interface Serializer {

	default public String serialize (RestStructure structure) {
		return serialize (structure, Collections.emptyMap ());
	}

	default public void serialize (RestStructure structure, Writer writer) {
		serialize (structure, Collections.emptyMap (), writer);
	}

	public String serialize (RestStructure structure, Map<String, String> attributes);
	public void serialize (RestStructure structure, Map<String, String> attributes, Writer writer);

}
