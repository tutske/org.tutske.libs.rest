package org.tutske.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


import static org.tutske.websocket.HttpRequest.*;


public class RoutingHandler extends AbstractHandler {

	private final UrlRouter router;
	private final Gson gson;

	public RoutingHandler (UrlRouter router, Gson gson) {
		this.router = router;
		this.gson = gson;
	}

	@Override public void handle (
		String target, Request baserequest, HttpServletRequest request,
		HttpServletResponse response
	) throws IOException, ServletException {
		Method method = Method.valueOf (request.getMethod ());
		UrlRoute route = router.route (method, target);
		RestObject result = route.getHandler ().apply (null);

		response.setContentType ("application/json");
		response.setStatus (HttpServletResponse.SC_OK);

		gson.toJson (result.asJson (), response.getWriter ());
		baserequest.setHandled (true);
	}

}
