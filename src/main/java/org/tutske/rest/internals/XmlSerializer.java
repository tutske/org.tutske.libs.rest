package org.tutske.rest.internals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tutske.rest.data.RestArray;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.data.RestStructure;

import java.io.Writer;
import java.util.Map.Entry;


public class XmlSerializer implements Serializer {

	private static final Logger logger = LoggerFactory.getLogger (XmlSerializer.class);
	private int level = 0;

	@Override
	public void serialize (RestStructure object, Writer writer) {
		try { writer.write (serialize (object)); }
		catch (Exception exception) {
			logger.debug ("could not write serialization to writer", exception);
			throw new RuntimeException (exception);
		}
	}

	@Override
	public String serialize (RestStructure structure) {
		if ( ! hasTag (structure) ) {
			throw new RuntimeException ();
		}
		level = 0;
		return // "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			serializeInto (structure, structure.getTag ());
	}

	private String serializeInto (RestStructure structure, String tagname) {
		String result = indent () + "<" + tagname;
		for ( Entry<String, Object> attribute : structure.getAttributes ().entrySet () ) {
			result += " " + attribute.getKey () + "=\"" + attribute.getValue () + "\"";
		}

		level++;
		String internal = serializeInternal (structure);
		level--;

		if ( internal.isEmpty () ) {
			return result + " />\n";
		} else {
			return  result + ">\n" + internal + indent () + "</" + tagname + ">\n";
		}
	}

	private String serializeInternal (RestStructure structure) {
		if ( structure instanceof RestObject ) {
			return serializeInternal ((RestObject) structure);
		} else if ( structure instanceof RestArray ) {
			return serializeInternal ((RestArray) structure);
		}
		return null;
	}

	private String serializeInternal (RestArray array) {
		String result = "";

		String start = hasChildTag (array) ? "<" + array.getChildTag () + ">" : "";
		String end = hasChildTag (array) ? "</" + array.getChildTag () + ">" : "";

		for ( Object object : array ) {
			if ( object instanceof RestStructure ) {
				RestStructure r = (RestStructure) object;
				if ( ! hasChildTag (array) && ! hasTag (r) ) {
					if ( r.getAttributes ().size () > 0 ) {
						throw new RuntimeException ("Noting to add attributes to");
					}
					result += serializeInternal (r);
				} else if ( hasChildTag (array) && hasTag (r) ) {
					result += indent () + start + "\n";
					level++;
					result += serialize (r);
					level--;
					result += indent () + end + "\n";
				} else if ( hasChildTag (array) ) {
					result += serializeInto (r, array.getChildTag ());
				} else {
					result += serialize (r);
				}
			} else {
				result += indent () + start;
				result += serializePrimitive (object);
				result += end;
			}
		}
		return result;
	}

	private String serializeInternal (RestObject object) {
		String result = "";
		for ( Entry<String, Object> entry : object.entrySet () ) {
			if ( entry.getKey ().equals ("$attributes") ) {
				continue;
			}
			Object value = entry.getValue ();
			if ( value instanceof RestStructure ) {
				RestStructure r = (RestStructure) value;
				if ( hasTag (r) ) {
					result += indent () + "<" + entry.getKey () + ">\n";
					level++;
					result += serialize (r);
					level--;
					result += indent () + "</" + entry.getKey () + ">\n";
				} else {
					result += serializeInto (r, entry.getKey ());
				}
			} else {
				result += indent () + "<" + entry.getKey () + ">";
				result += serializePrimitive (value);
				result += "</" + entry.getKey () + ">" + "\n";
			}
		}
		return result;
	}

	private String serializePrimitive (Object primitive) {
		return primitive.toString ();
	}

	private boolean hasTag (RestStructure structure) {
		return structure.getTag () != null && ! structure.getTag ().equals (".");
	}

	private boolean hasChildTag (RestArray array) {
		return array.getChildTag () != null && ! array.getChildTag ().equals (".");
	}

	private String indent () {
		String tabs = "";
		for (int i = 0; i < level; i++ ) {
			tabs += "\t";
		}
		return tabs;
	}
}
