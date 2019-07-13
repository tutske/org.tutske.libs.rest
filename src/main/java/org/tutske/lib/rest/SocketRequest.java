package org.tutske.lib.rest;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.tutske.lib.api.Request;
import org.tutske.lib.utils.Bag;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;


class SocketRequest implements Request {

	public static Bag<String, String> extractHeaders (ServletUpgradeRequest request) {
		return extractHeadersInto (new Bag<> (), request);
	}

	public static Bag<String, String> extractHeadersInto (Bag<String, String> headers, ServletUpgradeRequest request) {
		Map<String, List<String>> original = request.getHeaders ();

		for ( Map.Entry<String, List<String>> entry : original.entrySet () ) {
			for ( String value : entry.getValue () ) { headers.add (entry.getKey (), value); }
		}

		return headers;
	}

	protected final ServletUpgradeRequest request;
	protected final ServletUpgradeResponse response;
	protected final Bag<String, String> path;
	protected final Bag<String, String> queryParams = new Bag<> ();
	protected final Bag<String, String> headers = new Bag<> ();
	protected final Bag<String, Object> context = new Bag<> ();

	public SocketRequest (ServletUpgradeRequest request, ServletUpgradeResponse response, Bag<String, String> path) {
		this.request = request;
		this.response = response;
		this.path = path;

		extractHeadersInto (headers, request);
	}

	@Override
	public Method getMethod () {
		return Method.of (request.getMethod ());
	}

	@Override
	public String getUri () {
		return request.getHttpServletRequest ().getRequestURI ();
	}

	@Override
	public void setHeader (String header, String value) {
		response.setHeader (header, value);
	}

	@Override
	public void setStatus (int status) {
		response.setStatusCode (status);
	}

	@Override
	public Bag<String, String> pathParams () {
		return path;
	}

	@Override
	public Bag<String, String> queryParams () {
		if ( queryParams.isEmpty () ) {
			Request.decodeInto (queryParams, request.getQueryString ());
		}
		return queryParams;
	}

	@Override
	public Bag<String, String> headers () {
		return headers;
	}

	@Override
	public Bag<String, Object> context () {
		return context;
	}

	@Override
	public <T> T json (Class<T> clazz) {
		throw new RuntimeException ("Socket upgrade requests don't have an inputstream");
	}

	@Override
	public InputStream getInputStream () throws IOException {
		throw new IOException ("Socket upgrade requests don't have an inputstream");
	}

	@Override
	public OutputStream getOutputStream () throws IOException {
		throw new IOException ("Socket upgrade requests don't have an outputstream");
	}

	@Override
	public <T> T extractWrapped (Class<T> clazz) {
		if ( ServletUpgradeRequest.class.equals (clazz) ) { return (T) request; }
		if ( ServletUpgradeResponse.class.equals (clazz) ) { return (T) response; }
		if ( HttpServletRequest.class.equals (clazz) ) { return (T) request.getHttpServletRequest (); }
		return null;
	}

}
