package org.tutske.rest;

import org.tutske.rest.internals.QueryStringDecoder;
import org.tutske.utils.Bag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class HttpRequest {

	public enum Method {
		CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE, UNKNOWN;
		public static Method of (String value) {
			try { return Method.valueOf (value.toUpperCase ()); }
			catch ( IllegalArgumentException e ) { return UNKNOWN; }
		}
	}

	private final Bag<String, Object> context = new Bag<> ();
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final Bag<String, String> path;
	private final Bag<String, String> queryParams;

	public HttpRequest (HttpServletRequest request, HttpServletResponse response, Bag<String, String> path) {
		this.request = request;
		this.response = response;
		this.path = path;
		this.queryParams = new Bag<> ();
	}

	public HttpServletRequest getServletRequest () {
		return request;
	}

	public HttpServletResponse getServletResponse () {
		return response;
	}

	public Method getMethod () {
		return Method.of (request.getMethod ());
	}

	public String getHeader (String header) {
		return request.getHeader (header);
	}

	public String getUri () {
		return request.getRequestURI ();
	}

	public Bag<String, String> pathParams () {
		return path;
	}

	public Bag<String, String> queryParams () {
		if ( queryParams.isEmpty () ) {
			QueryStringDecoder.decodeInto (queryParams, request.getQueryString ());
		}
		return queryParams;
	}

	public Bag<String, Object> context () {
		return this.context;
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

}
