package org.tutske.rest.internals;

import org.tutske.rest.ParameterBag;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class QueryStringDecoder {

	public static void decodeInto (ParameterBag bag, String querystring) {
		if ( querystring == null || querystring.isEmpty () ) { return; }

		for ( String part : querystring.split ("&") ) {
			String [] split = part.split ("=", 2);
			String key = decodeQueryString (split[0]);

			if ( split.length == 2 && ! split[1].isEmpty () ) {
				String value = decodeQueryString (split[1]);
				bag.add (key, value);
			} else {
				bag.add (key);
			}
		}
	}

	public static ParameterBag decode (String querystring) {
		ParameterBag bag = new ParameterBag ();
		decodeInto (bag, querystring);
		return bag;
	}

	private static String decodeQueryString (String encoded) {
		try { return URLDecoder.decode (encoded, "UTF-8"); }
		catch ( UnsupportedEncodingException exception ) {
			throw new RuntimeException ("NO UTF-8 support?", exception);
		}
	}

}
