package org.tutske.rest.internals;

import static org.tutske.rest.HttpRequest.Method;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.tutske.rest.*;
import org.tutske.rest.data.RestStructure;
import org.tutske.rest.routes.UrlRoute;
import org.tutske.utils.Bag;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class RestHandler extends AbstractHandler {

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
		Method method = Method.of (request.getMethod ());
		String identifier = router.route (method, s);
		UrlRoute<ControllerFunction> route = router.find (identifier);

		if ( route == null ) { return; }

		ControllerFunction fn = route.getHandler (identifier);
		Bag<String, String> data = route.extractMatches (identifier, s, s.substring (1).split ("/"));
		HttpRequest r = new HttpRequest (request, response, data);
		request.setAttribute ("context", r.context ());

		RestStructure result;
		try { result = filters.createChain (s, fn).call (r); }
		catch ( RuntimeException e ) { throw e; }
		catch ( Exception e ) { throw new RuntimeException (e); }

		int status = response.getStatus () == 0 ? HttpServletResponse.SC_OK : response.getStatus ();
		String contentType = response.getContentType ();

		String accept = contentType != null ? contentType : request.getHeader ("Accept");
		contentType = serializer.contentType (accept, result);

		response.setContentType (contentType);
		response.setStatus (status);
		serializer.serialize (accept, result, response.getOutputStream ());
		response.getOutputStream ().flush ();

		base.setHandled (true);
	}

}
