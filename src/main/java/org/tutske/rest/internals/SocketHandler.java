package org.tutske.rest.internals;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tutske.rest.*;
import org.tutske.utils.Bag;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.tutske.rest.HttpRequest.*;


public class SocketHandler extends WebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger (SocketHandler.class);
	private final UrlRouter<SocketFunction> router;

	public SocketHandler (UrlRouter<SocketFunction> router) {
		this.router = router;
	}

	private final ThreadLocal<UrlRoute<SocketFunction>> route = new ThreadLocal<> ();

	@Override
	public void configure (WebSocketServletFactory factory) {
		factory.setCreator ((request, response) -> {
			String pathstring = request.getHttpServletRequest ().getRequestURI ();
			UrlRoute<SocketFunction> route = this.route.get ();
			SocketFunction function = route.getHandler ();

			Bag<String, String> path = route.extractMatches (pathstring, pathstring.substring (1).split ("/"));
			SocketRequest socketRequest = new SocketRequest (request, response, path);

			try {
				return new WebSocketListenerWrapper (function.apply (socketRequest));
			} catch (Throwable exception) {
				logger.debug ("creating web socket failed", exception);
				throw exception;
			}
		});
	}

	@Override
	public void handle (String s, Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		Method method = Method.valueOf (base.getMethod ());
		UrlRoute<SocketFunction> route = router.route (method, s);

		if ( route == null ) { return; }

		this.route.set (route);
		super.handle (s, base, request, response);
	}

}
