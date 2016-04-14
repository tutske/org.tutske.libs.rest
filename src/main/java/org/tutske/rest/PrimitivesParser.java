package org.tutske.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class PrimitivesParser {

	private static final String ERROR_FORMAT = "Clazz not supported for conversion: %s (%s)";

	private static final Map<Class<?>, Function<String, ?>> converters = new HashMap<> ();

	static {
		converters.put (String.class, Function.identity ());
		converters.put (Integer.class, Integer::parseInt);
		converters.put (Long.class, Long::parseLong);
		converters.put (Float.class, Float::parseFloat);
		converters.put (Double.class, Double::parseDouble);
		converters.put (Boolean.class, Boolean::parseBoolean);
	}

	public static <T> T parse (String value, Class<T> clazz) {
		if ( ! converters.containsKey (clazz) ) {
			String msg = String.format (ERROR_FORMAT, value, clazz);
			throw new RuntimeException (msg);
		}

		return (T) converters.get (clazz).apply (value);
	}

}
