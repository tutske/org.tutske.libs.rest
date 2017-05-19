package org.tutske.rest;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.tutske.rest.internals.QueryStringDecoder;
import org.tutske.utils.Bag;

import javax.servlet.http.HttpServletRequest;


public class SocketRequest {

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


	public HttpServletRequest getServletRequest () {
		return request.getHttpServletRequest ();
	}

	public ServletUpgradeRequest getSocketRequest () {
		return request;
	}

	public ServletUpgradeResponse getSocketResponse () {
		return response;
	}

	public String getHeader (String header) {
		return request.getHeader (header);
	}

	public String getUri () {
		return getServletRequest ().getRequestURI ();
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

}
