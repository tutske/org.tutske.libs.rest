package org.tutske.rest.internals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class PrimitivesParser {

	private static final SimpleDateFormat [] formats = new SimpleDateFormat [] {
		new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss'Z'Z"),
		new SimpleDateFormat ("yyyy/MM/dd"),
		new SimpleDateFormat ("dd/MM/yyyy"),
		new SimpleDateFormat ("dd/MM/yy"),
	};

	private static final String ERROR_FORMAT = "Clazz not supported for conversion: %s (%s)";

	private static final Map<Class<?>, Function<String, ?>> converters = new HashMap<> ();

	static {
		converters.put (String.class, Function.identity ());
		converters.put (Integer.class, Integer::parseInt);
		converters.put (Long.class, Long::parseLong);
		converters.put (Float.class, Float::parseFloat);
		converters.put (Double.class, Double::parseDouble);
		converters.put (Boolean.class, Boolean::parseBoolean);
		converters.put (Date.class, PrimitivesParser::parseDate);
	}

	public static <T> T parse (String value, Class<T> clazz) {
		if ( ! converters.containsKey (clazz) ) {
			String msg = String.format (ERROR_FORMAT, value, clazz);
			throw new RuntimeException (msg);
		}

		return (T) converters.get (clazz).apply (value);
	}

	public static Date parseDate (String value) {
		for ( SimpleDateFormat format : formats ) {
			try { return format.parse (value); }
			catch ( ParseException ignore ) {}
		}

		try { return new Date (Long.parseLong (value)); }
		catch ( NumberFormatException ignore ) {}

		throw new IllegalArgumentException ("Not a valid date representation: `" + value + "`");
	}

}
