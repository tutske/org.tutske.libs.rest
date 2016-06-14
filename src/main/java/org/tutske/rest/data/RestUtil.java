package org.tutske.rest.data;

public class RestUtil {

	public static void assureValid (Object object) {
		if ( ! isValid (object) ) {
			throw new RuntimeException ("Not a valid object: " + object);
		}
	}

	public static boolean isValid (Object object) {
		return (object instanceof RestStructure) || isPrimitive (object);
	}

	public static void assurePrimitive (Object object) {
		if ( ! isPrimitive (object) ) {
			throw new RuntimeException ("Not a primitive value: " + object);
		}
	}

	public static boolean isPrimitive (Object object) {
		return (object instanceof Boolean) || (object instanceof Number) || (object instanceof String);
	}

}
