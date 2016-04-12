package org.tutske.rest;

public class RestUtil {

	public static void assureValid (Object object) {
		if ( ! isValid (object) ) {
			throw new RuntimeException ("Not a valid object: " + object);
		}
	}

	public static boolean isValid (Object object) {
		return (object instanceof RestArray) || (object instanceof RestObject) ||
			(object instanceof Boolean) || (object instanceof Number) ||
			(object instanceof String);
	}

}
