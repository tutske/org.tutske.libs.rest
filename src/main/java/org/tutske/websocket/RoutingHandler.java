package org.tutske.websocket;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.tutske.websocket.HttpRequest.*;


public class RoutingHandler extends AbstractHandler {

	private final UrlRouter router;

	public RoutingHandler (UrlRouter router) {
		this.router = router;
	}

	@Override public void handle (
		String target, Request baserequest, HttpServletRequest request,
		HttpServletResponse response
	) throws IOException, ServletException {
		Method method = Method.valueOf (request.getMethod ());

		UrlRoute route = router.route (method, target);
		RestResponse result = route.getHandler ().apply (null);

		response.setContentType ("text");
		response.setStatus (HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter ();
		out.println (result.toString ());
		baserequest.setHandled (true);

		System.out.println ("Trying again");
	}

}
