package org.tutske.rest.internals;

import org.tutske.rest.data.RestStructure;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


public class ContentSerializer {

	private final Map<String, Serializer> serializers;
	private final String defaultType;

	public ContentSerializer (String defaultType, Map<String, Serializer> serializers) {
		this.serializers = serializers;
		this.defaultType = defaultType;
	}

	public ContentSerializer (Map<String, Serializer> serializers) {
		this (null, serializers);
	}

	public String contentType (String accept) {
		String favourite = pickFavourite (accept);
		return extractType (favourite);
	}

	public String serialize (String accept, RestStructure structure) {
		String mime = pickFavourite (accept);
		String type = extractType (mime);
		Map<String, String> attributes = extractAttributes (mime);
		return serializers.get (type).serialize (structure, attributes);
	}

	public void serialize (String accept, RestStructure structure, Writer writer) {
		String mime = pickFavourite (accept);
		String type = extractType (mime);
		Map<String, String> attributes = extractAttributes (mime);
		serializers.get (type).serialize (structure, attributes, writer);
	}

	private String pickFavourite (String accept) {
		int index = 0;

		while ( index < accept.length () ) {
			int end = nextMime (accept, index);
			String choice = accept.substring (index, end);
			index = end + 1;
			String type = extractType (choice).trim ();
			if ( serializers.containsKey (type) ) {
				return choice.trim ();
			}
		}

		if ( defaultType != null ) { return defaultType; }

		throw new RuntimeException ("Could not find type from requested: " + accept);
	}

	private String extractType (String mime) {
		int end = mime.indexOf (";");
		return end < 0 ? mime : mime.substring (0, end);
	}

	private Map<String, String> extractAttributes (String mime) {
		Map<String, String> attributes = new HashMap<> ();

		int index = mime.indexOf (";") + 1;
		if ( index == 0 ) { return attributes; }

		while ( index < mime.length () ) {
			int end = nextAttribute (mime, index);
			String part = mime.substring (index, end);
			index = end + 1;

			String [] pair = part.split ("=", 2);
			attributes.put (pair[0].trim (), extractAttributeValue (pair[1]));
		}

		return attributes;
	}

	private String extractAttributeValue (String valuestring) {
		valuestring = valuestring.trim ();
		if (valuestring.endsWith ("\"") && valuestring.endsWith ("\"") ) {
			valuestring = valuestring.substring (1, valuestring.length () - 1);
		}
		return valuestring;
	}

	private int nextAttribute (String value, int index) {
		int semicolon = value.indexOf (";", index);
		int quote = value.indexOf ("\"", index);

		if ( quote > -1 && quote < semicolon ) {
			quote = value.indexOf ("\"", quote + 1);
			semicolon = value.indexOf (";", quote);
		}

		return semicolon < 0 ? value.length () : semicolon;
	}

	private int nextMime (String value, int index) {
		int colon = value.indexOf (",", index);
		int quote = value.indexOf ("\"", index);
		boolean closed = quote > -1;

		while ( quote > -1 && quote < colon ) {
			quote = value.indexOf ("\"", quote + 1);
			closed = ! closed;
			if ( quote > colon && ! closed ) {
				colon = value.indexOf (",", quote);
			}
		}

		return colon < 0 ? value.length () : colon;
	}

}
