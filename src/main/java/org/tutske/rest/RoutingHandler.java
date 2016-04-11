package org.tutske.rest;

import static org.tutske.rest.HttpRequest.Method;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class RoutingHandler extends AbstractHandler {

	private final UrlRouter router;
	private final Gson gson;

	public RoutingHandler (UrlRouter router, Gson gson) {
		this.router = router;
		this.gson = gson;
	}

	@Override
	public void handle (String s, Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		Method method = Method.valueOf (request.getMethod ());
		UrlRoute route = router.route (method, s);

		if ( route == null ) {
			return;
		}

		Object result;
		try {
			result = route.getHandler ().apply (null).asJson ();
		} catch (ResponseException e) {
			result = e.asJson ();
		} catch (Exception e) {
			result = new ResponseException (e.getMessage ()).asJson ();
		}

		response.setContentType ("application/json");
		response.setStatus (HttpServletResponse.SC_OK);
		gson.toJson (result, response.getWriter ());

		base.setHandled (true);
	}

}
