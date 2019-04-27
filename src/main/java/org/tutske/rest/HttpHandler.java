package org.tutske.rest;

import static org.tutske.rest.Request.Method;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.tutske.lib.utils.Bag;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Consumer;


public class HttpHandler extends AbstractHandler implements Consumer<ApiRouter<Request, Object>> {

	private final ObjectMapper mapper;
	private final ExceptionResponder responder;
	private ApiRouter<Request, Object> router;

	public HttpHandler () { this (null, null, null); }
	public HttpHandler (ApiRouter<Request, Object> router) { this (router, null, null); }
	public HttpHandler (ObjectMapper mapper) { this (null, mapper, null); }

	public HttpHandler (ApiRouter<Request, Object> router, ObjectMapper mapper, ExceptionResponder responder) {
		this.mapper = mapper != null ? mapper : new ObjectMapper ()
			.disable (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable (SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.registerModule (new Jdk8Module ())
			.registerModule (new JavaTimeModule ());

		this.responder = new ExceptionResponder (this.mapper);
		this.router = router;
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

		String [] parts = API.splitParts (base.getHttpURI ().getPath ());
		Method method = Method.of (request.getMethod ());

		String identifier = router.toId (method, "current", s, parts);

		if ( identifier == null ) { return; }

		Bag<String, String> data = router.extractMatches (identifier, s, parts);
		HttpRequest r = new HttpRequest (request, response, data, mapper);
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
