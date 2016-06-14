package org.tutske.rest.internals;

import org.tutske.rest.data.RestStructure;

import java.io.Writer;


public interface Serializer {

	public String serialize (RestStructure object);
	public void serialize (RestStructure object, Writer writer);

}
