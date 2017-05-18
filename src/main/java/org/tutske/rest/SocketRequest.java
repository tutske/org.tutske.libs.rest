package org.tutske.rest;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.tutske.rest.internals.QueryStringDecoder;

import javax.servlet.http.HttpServletRequest;


public class SocketRequest {

	private final ServletUpgradeRequest request;
	private final ServletUpgradeResponse response;
	private final ParameterBag<String> path;
	private final ParameterBag<String> queryParams;

	public SocketRequest (ServletUpgradeRequest request, ServletUpgradeResponse response, ParameterBag<String> path) {
		this.request = request;
		this.response = response;
		this.path = path;
		this.queryParams = new ParameterBag<> ();
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

	public ParameterBag<String> pathParams () {
		return path;
	}

	public ParameterBag<String> queryParams () {
		if ( queryParams.isEmpty () ) {
			QueryStringDecoder.decodeInto (queryParams, request.getQueryString ());
		}
		return queryParams;
	}

}
