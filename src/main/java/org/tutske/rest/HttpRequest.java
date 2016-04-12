package org.tutske.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;


public class HttpRequest {

	public enum Method {
		HEAD, OPTIONS, GET, POST, PUT, DELETE, TRACE
	}

	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final Map<String, String> path;
	private final Map<String, List<String>> query;

	public HttpRequest (HttpServletRequest request, HttpServletResponse response, Map<String, String> path) {
		this.request = request;
		this.response = response;
		this.path = path;
		this.query = new HashMap<String, List<String>> ();
	}

	public HttpServletRequest getServletRequest () {
		return request;
	}

	public HttpServletResponse getServletResponse () {
		return response;
	}

	public Method getMethod () {
		return Method.valueOf (request.getMethod ());
	}

	public String getHeader (String header) {
		return request.getHeader (header);
	}

	public String getUri () {
		return request.getRequestURI ();
	}

	public String getPathParameter (String name) {
		return getPathParameter (name, String.class);
	}

	public <T> T getPathParameter (String name, Class<T> clazz) {
		return (T) path.get (name);
	}

	public String getQueryParameter (String name) {
		return getQueryParameter (name, String.class);
	}

	public <T> T getQueryParameter (String name, Class<T> clazz) {
		if ( query.isEmpty () ) { parseQueryString (); }
		if ( ! query.containsKey (name) ) { return null; }
		return (T) parse (query.get (name).get (0), clazz);
	}

	public String getBody () {
		try {
			StringBuilder builder = new StringBuilder ();
			Scanner scanner = new Scanner (getInputStream (), "utf-8");
			while ( scanner.hasNextLine () ) {
				builder.append (scanner.nextLine ()).append ("\n");
			}
			return builder.toString ();
		} catch (IOException exceptino) {
			return "";
		}
	}

	public InputStream getInputStream () throws IOException {
		return request.getInputStream ();
	}

	private void parseQueryString () {
		String queryString = request.getQueryString ();
		if ( queryString == null || queryString.isEmpty () ) { return; }

		for ( String part : queryString.split ("&") ) {
			String [] split = part.split ("=", 2);
			String key = URLDecoder.decode (split[0]);

			if ( ! query.containsKey (key) ) {
				query.put (key, new LinkedList<String> ());
			}

			if ( split.length == 2 && ! split[1].isEmpty () ) {
				String value = URLDecoder.decode (split[1]);
				query.get (key).add (value);
			}
		}
	}

	private Object parse (String value, Class<?> clazz) {
		if ( String.class.equals (clazz) ) {
			return value;
		} else if ( Integer.class.equals (clazz) ) {
			return Integer.parseInt (value);
		} else if ( Float.class.equals (clazz) ) {
			return Float.parseFloat (value);
		} else if ( Boolean.class.equals (clazz) ) {
			return Boolean.parseBoolean (value);
		}
		String msg = "Clazz not supported for convertion: " + value + " (" + clazz + ")";
		throw new RuntimeException (msg);
	}

}
