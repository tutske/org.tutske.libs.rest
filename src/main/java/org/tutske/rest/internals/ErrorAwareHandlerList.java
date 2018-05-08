package org.tutske.rest.internals;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tutske.rest.data.RestStructure;
import org.tutske.rest.exceptions.ResponseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class ErrorAwareHandlerList extends HandlerList {

	private final static Logger logger = LoggerFactory.getLogger (RestHandler.class);

	private final ContentSerializer serializer;

	public ErrorAwareHandlerList (Handler ... handlers) {
		this (new ContentSerializer ("application/json") {{
			put ("application/json", new JsonSerializer ());
			put ("application/xml", new XmlSerializer ());
		}}, handlers);
	}

	public ErrorAwareHandlerList (ContentSerializer serializer, Handler ... handlers) {
		for ( Handler handler : handlers ) { addHandler (handler); }
		this.serializer = serializer;
	}

	@Override
	public void handle (String s, Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		ResponseException exception;
		try {
			super.handle (s, base, request, response);
			return;
		} catch (ResponseException ex) {
			exception = ex;
		} catch (Exception ex) {
			String msg = ex.getMessage ();
			exception = new ResponseException (msg == null ? "" : msg) {{
				status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			}};
		}

		logger.warn ("Failed to serve a request, {}, {}", exception.getStatusCode (), s, exception);
		String host = request.getHeader ("host");
		String uri = host == null || host.isEmpty () ? null : request.getScheme () + "://" + host;

		RestStructure result = exception.asRestStructure (uri);
		String accept = request.getHeader ("Accept");
		String contentType = serializer.contentType (accept, result);

		response.setContentType (contentType);
		response.setStatus (exception.getStatusCode ());
		serializer.serialize (accept, result, response.getOutputStream ());
		response.getOutputStream ().flush ();

		base.setHandled (true);
	}

}
