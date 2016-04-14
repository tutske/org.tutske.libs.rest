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
	private final ParameterBag path;
	private final ParameterBag queryParams;

	public HttpRequest (HttpServletRequest request, HttpServletResponse response, ParameterBag path) {
		this.request = request;
		this.response = response;
		this.path = path;
		this.queryParams = new ParameterBag ();
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

	public ParameterBag pathParams () {
		return path;
	}

	public ParameterBag queryParams () {
		if ( queryParams.isEmpty () ) { parseQueryString (); }
		return queryParams;
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
			String key = decodeQueryString (split[0]);

			if ( split.length == 2 && ! split[1].isEmpty () ) {
				String value = decodeQueryString (split[1]);
				queryParams.add (key, value);
			}
		}
	}

	private String decodeQueryString (String encoded) {
		try { return URLDecoder.decode (encoded, "UTF-8"); }
		catch ( UnsupportedEncodingException exception ) {
			throw new RuntimeException ("NO UTF-8 support?", exception);
		}
	}

}
