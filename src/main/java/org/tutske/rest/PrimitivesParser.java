package org.tutske.rest;

public class PrimitivesParser {

	private static final String ERROR_FORMAT = "Clazz not supported for conversion: %s (%s)";

	public static <T> T parse (String value, Class<T> clazz) {
		Object result = null;

		if ( String.class.equals (clazz) ) {
			result = value;
		} else if ( Integer.class.equals (clazz) ) {
			result = Integer.parseInt (value);
		} else if ( Float.class.equals (clazz) ) {
			result = Float.parseFloat (value);
		} else if ( Boolean.class.equals (clazz) ) {
			result = Boolean.parseBoolean (value);
		}

		if ( result == null ) {
			String msg = String.format (ERROR_FORMAT, value, clazz);
			throw new RuntimeException (msg);
		}

		return (T) value;
	}

}
