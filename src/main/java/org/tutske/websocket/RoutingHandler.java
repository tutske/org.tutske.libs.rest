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

		if ( route == null ) {
			return;
		}

		try { handleResponse (request, response, route.getHandler ().apply (null)); }
		catch ( IOException io ) { throw io; }
		catch ( Exception e ) { handleException (request, response, e); }

		baserequest.setHandled (true);
	}

	private void handleResponse (HttpServletRequest request, HttpServletResponse response, RestObject result)
	throws IOException {
		response.setContentType ("application/json");
		response.setStatus (HttpServletResponse.SC_OK);

		gson.toJson (result.asJson (), response.getWriter ());
	}

	private void handleException (HttpServletRequest request, HttpServletResponse response, Exception exception)
	throws IOException {
		response.setContentType ("application/json");
		response.setStatus (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		RestObject result = new RestObject () {{
			v ("type", "http://www.example.com/internal_server_error");
			v ("title", "Internal Error");
			v ("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			v ("detail", exception.getMessage ());
		}};

		gson.toJson (result.asJson (), response.getWriter ());
	}

}
