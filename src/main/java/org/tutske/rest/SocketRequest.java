package org.tutske.rest;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.tutske.utils.Bag;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


class SocketRequest implements Request {

	private final Bag<String, Object> context = new Bag<> ();
	private final ServletUpgradeRequest request;
	private final ServletUpgradeResponse response;
	private final Bag<String, String> path;
	private final Bag<String, String> queryParams;

	public SocketRequest (ServletUpgradeRequest request, ServletUpgradeResponse response, Bag<String, String> path) {
		this.request = request;
		this.response = response;
		this.path = path;
		this.queryParams = new Bag<> ();
	}

	@Override
	public Method getMethod () {
		return Method.of (request.getMethod ());
	}

	@Override
	public String getHeader (String header) {
		return request.getHeader (header);
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
