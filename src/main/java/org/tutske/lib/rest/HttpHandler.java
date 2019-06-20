package org.tutske.lib.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.tutske.lib.api.API;
import org.tutske.lib.api.ApiRouter;
import org.tutske.lib.api.Request;
import org.tutske.lib.utils.Bag;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Consumer;


public class HttpHandler extends AbstractHandler implements Consumer<ApiRouter<Request, Object>> {

	@FunctionalInterface
	public static interface RequestSupplier {
		public Request get (HttpServletRequest request, HttpServletResponse response, Bag<String, String> path);
	}

	private final ObjectMapper mapper;
	private final ExceptionResponder responder;
	private final RequestSupplier requests;
	private ApiRouter<Request, Object> router;

	public HttpHandler () { this (null, null); }
	public HttpHandler (ObjectMapper mapper) { this (mapper, null); }
	public HttpHandler (RequestSupplier requests) { this (null, requests); }

	public HttpHandler (ObjectMapper mapper, RequestSupplier requests) {
		this.mapper = mapper != null ? mapper : new ObjectMapper ()
			.disable (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable (SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.registerModule (new Jdk8Module ())
			.registerModule (new JavaTimeModule ());

		this.responder = new ExceptionResponder (this.mapper);
		this.requests = requests != null ? requests : (req, res, path) -> new HttpRequest (req, res, path, mapper);
	}

	@Override
	public void accept (ApiRouter<Request, Object> router) {
		if ( this.router == null ) { this.router = router; }
	}

	@Override
	public void handle (String s, org.eclipse.jetty.server.Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		try { riskyHandle (s, base, request, response); }
		catch ( Exception e ) {
			responder.respond (s, base, request, response, e);
			base.setHandled (true);
		}
	}

	public void riskyHandle (String s, org.eclipse.jetty.server.Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		if ( router == null ) {
			throw new RuntimeException ("Http handler cannot handle requests when no router is configured");
		}

		String [] parts = API.saveSplitParts (base.getHttpURI ().getPath ());
		Request.Method method = Request.Method.of (request.getMethod ());

		String identifier = router.toId (method, "current", s, parts);

		if ( identifier == null ) { return; }

		Bag<String, String> data = router.extractMatches (identifier, s, parts);
		Request r = requests.get (request, response, data);
		request.setAttribute ("context", r.context ());

		Object result;
		try { result = router.createChain (method, "current", s, parts).apply (r); }
		catch ( RuntimeException e ) { throw e; }
		catch ( Exception e ) { throw new RuntimeException (e); }

		int status = response.getStatus () == 0 ? HttpServletResponse.SC_OK : response.getStatus ();

		response.setStatus (status);
		mapper.writeValue (response.getOutputStream (), result);
		response.getOutputStream ().flush ();

		base.setHandled (true);
	}

}
