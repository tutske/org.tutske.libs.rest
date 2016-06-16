package org.tutske.rest.internals;

import static org.tutske.rest.HttpRequest.Method;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tutske.rest.*;
import org.tutske.rest.data.RestStructure;
import org.tutske.rest.exceptions.ResponseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class RestHandler extends AbstractHandler {

	private final static Logger logger = LoggerFactory.getLogger (RestHandler.class);

	private final UrlRouter<ControllerFunction> router;
	private final FilterCollection<HttpRequest, RestStructure> filters;
	private final Map<String, Serializer> serializers;

	public RestHandler (UrlRouter<ControllerFunction> router,
		FilterCollection<HttpRequest, RestStructure> filters,
		Map<String, Serializer> serializers
	) {
		this.router = router;
		this.filters = filters;
		this.serializers = serializers;
	}

	public RestHandler (UrlRouter<ControllerFunction> router, Map<String, Serializer> serializers) {
		this (router, new FilterCollection<> (), serializers);
	}

	public RestHandler (UrlRouter<ControllerFunction> router) {
		this (router, new HashMap<String, Serializer> () {{
			put ("application/json", new JsonSerializer ());
			put ("application/xml", new XmlSerializer ());
			put ("default", get ("application/json"));
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
		Object result;
		try {
			ParameterBag data = route.extractMatches (s, s.substring (1).split ("/"));
			HttpRequest r = new HttpRequest (request, response, data);
			result = route.getHandler ().apply (r);
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
		String type = serializers.containsKey (accept) ? accept : "application/json";
		Serializer serializer = serializers.get (serializers.containsKey (accept) ? accept : "default");

		response.setContentType (type);
		response.setStatus (status);

		serializer.serialize ((RestStructure) result, response.getWriter ());
		response.getWriter ().flush ();

		base.setHandled (true);
	}

}
