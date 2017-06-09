package org.tutske.rest.internals;

import static org.tutske.rest.HttpRequest.Method;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tutske.rest.*;
import org.tutske.rest.data.RestStructure;
import org.tutske.rest.exceptions.ResponseException;
import org.tutske.utils.Bag;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class RestHandler extends AbstractHandler {

	private final static Logger logger = LoggerFactory.getLogger (RestHandler.class);

	private final UrlRouter<ControllerFunction> router;
	private final FilterCollection<HttpRequest, RestStructure> filters;
	private final ContentSerializer serializer;

	public RestHandler (UrlRouter<ControllerFunction> router,
		FilterCollection<HttpRequest, RestStructure> filters,
		ContentSerializer serializer
	) {
		this.router = router;
		this.filters = filters;
		this.serializer = serializer;
	}

	public RestHandler (UrlRouter<ControllerFunction> router, ContentSerializer serializer) {
		this (router, new FilterCollection<> (), serializer);
	}

	public RestHandler (UrlRouter<ControllerFunction> router) {
		this (router, new ContentSerializer ("application/json") {{
			put ("application/json", new JsonSerializer ());
			put ("application/xml", new XmlSerializer ());
		}});
	}

	@Override
	public void handle (String s, Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		Method method = Method.valueOf (request.getMethod ());
		UrlRoute<ControllerFunction> route = router.route (method, s);

		if ( route == null ) {
			return;
		}

		int status = HttpServletResponse.SC_OK;
		RestStructure result;
		try {
			Bag<String, String> data = route.extractMatches (s, s.substring (1).split ("/"));
			HttpRequest r = new HttpRequest (request, response, data);
			result = filters.createChain (s, (rr) -> route.getHandler ().apply (rr)).call (r);
			if ( response.getStatus () != 0 ) {
				status = response.getStatus ();
			}
		} catch (ResponseException exception) {
			result = exception.asRestStructure ();
			status = exception.getStatusCode ();
		} catch (Exception exception) {
			logger.warn ("Failed to perform request", exception);
			String msg = exception.getMessage ();
			result = new ResponseException (msg == null ? "" : msg).asRestStructure ();
			status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}

		String accept = request.getHeader ("Accept");
		String contentType = serializer.contentType (accept, result);

		response.setContentType (contentType);
		response.setStatus (status);
		serializer.serialize (accept, result, response.getOutputStream ());
		response.getOutputStream ().flush ();

		base.setHandled (true);
	}

}
