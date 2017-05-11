package org.tutske.rest;

import org.tutske.rest.internals.QueryStringDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class HttpRequest {

	public enum Method {
		HEAD, OPTIONS, GET, POST, PUT, DELETE, TRACE
	}

	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final ParameterBag<String> path;
	private final ParameterBag<String> queryParams;

	public HttpRequest (HttpServletRequest request, HttpServletResponse response, ParameterBag<String> path) {
		this.request = request;
		this.response = response;
		this.path = path;
		this.queryParams = new ParameterBag<> ();
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

	public ParameterBag<String> pathParams () {
		return path;
	}

	public ParameterBag<String> queryParams () {
		if ( queryParams.isEmpty () ) {
			QueryStringDecoder.decodeInto (queryParams, request.getQueryString ());
		}
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

}
