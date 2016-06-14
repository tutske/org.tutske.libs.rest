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
		String result = serializeInto (structure, structure.getTag ());
		System.out.println (result);
		return result;
	}

	private String serializeInto (RestStructure structure, String tagname) {
		String result = "<" + tagname;
		for ( Entry<String, Object> attribute : structure.getAttributes ().entrySet () ) {
			result += " " + attribute.getKey () + "=\"" + attribute.getValue () + "\"";
		}

		String internal = serializeInternal (structure);

		if ( internal.isEmpty () ) {
			return result + " />";
		} else {
			return  result + ">" + internal + "</" + tagname + ">";
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
					result += start;
					result += serialize (r);
					result += end;
				} else if ( hasChildTag (array) ) {
					result += serializeInto (r, array.getChildTag ());
				} else {
					result += serialize (r);
				}
			} else {
				result += start;
				result += object;
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
					result += "<" + entry.getKey () + ">";
					result += serialize (r);
					result += "</" + entry.getKey () + ">";
				} else {
					result += serializeInto (r, entry.getKey ());
				}
			} else {
				result += "<" + entry.getKey () + ">";
				result += value;
				result += "</" + entry.getKey () + ">";
			}
		}
		return result;
	}

	private String serializePrimitive (Object primitive) {
		return "" + primitive;
	}

	private boolean hasTag (RestStructure structure) {
		return structure.getTag () != null && ! structure.getTag ().equals (".");
	}

	private boolean hasChildTag (RestArray array) {
		return array.getChildTag () != null && ! array.getChildTag ().equals (".");
	}

}
