package org.tutske.rest.internals;

import static org.tutske.rest.HttpRequest.Method;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tutske.rest.*;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.exceptions.ResponseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class RestHandler extends AbstractHandler {

	private final static Logger logger = LoggerFactory.getLogger (RestHandler.class);
	private final UrlRouter<ControllerFunction> router;
	private final Gson gson;

	public RestHandler (UrlRouter<ControllerFunction> router, FilterCollection<HttpRequest, RestObject> filters, Gson gson) {
		this.router = router;
		this.gson = gson;
	}

	public RestHandler (UrlRouter<ControllerFunction> router, Gson gson) {
		this.router = router;
		this.gson = gson;
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
			result = route.getHandler ().apply (r).asJson ();
		} catch (ResponseException exception) {
			result = exception.asJson ();
			status = exception.getStatusCode ();
		} catch (Exception exception) {
			logger.warn ("Failed to perform request", exception);
			String msg = exception.getMessage ();
			result = new ResponseException (msg == null ? "" : msg).asJson ();
			status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}

		response.setContentType ("application/json");
		response.setStatus (status);
		gson.toJson (result, response.getWriter ());

		base.setHandled (true);
	}

}
