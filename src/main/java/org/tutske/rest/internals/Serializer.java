package org.tutske.rest.internals;

import org.tutske.rest.data.RestStructure;

import java.io.Writer;


public interface Serializer {

	public String serialize (RestStructure structure);
	public void serialize (RestStructure structure, Writer writer);

}
