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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


public class HttpAsyncHandler extends AbstractHandler implements Consumer<ApiRouter<Request, CompletableFuture<Object>>> {

	private final ExecutorService workers = Executors.newCachedThreadPool ();
	private final ObjectMapper mapper;
	private final ExceptionResponder responder;
	private ApiRouter<Request, CompletableFuture<Object>> router;

	public HttpAsyncHandler () { this (null, null, null); }
	public HttpAsyncHandler (ApiRouter<Request, CompletableFuture<Object>> router) { this (router, null, null); }
	public HttpAsyncHandler (ObjectMapper mapper) { this (null, mapper, null); }

	public HttpAsyncHandler (ApiRouter<Request, CompletableFuture<Object>> router, ObjectMapper mapper, ExceptionResponder responder) {
		this.mapper = mapper != null ? mapper : new ObjectMapper ()
			.disable (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable (SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.registerModule (new Jdk8Module ())
			.registerModule (new JavaTimeModule ());

		this.responder = new ExceptionResponder (this.mapper);
		this.router = router;
	}

	@Override
	public void accept (ApiRouter<Request, CompletableFuture<Object>> router) {
		if ( this.router == null ) { this.router = router; }
	}

	@Override
	public void handle (String s, org.eclipse.jetty.server.Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		if ( router == null ) {
			throw new RuntimeException ("Http handler cannot handle requests when no router is configured");
		}

		String [] parts = API.splitParts (base.getHttpURI ().getPath ());
		Request.Method method = Request.Method.of (request.getMethod ());

		String identifier = router.toId (method, "current", s, parts);

		if ( identifier == null ) { return; }

		Bag<String, String> data = router.extractMatches (identifier, s, parts);
		HttpRequest r = new HttpRequest (request, response, data, mapper);
		request.setAttribute ("context", r.context ());

		CompletableFuture<Object> future = router.createChain (method, "current", s, parts).apply (r);
		future.whenCompleteAsync ((value, throwable) -> {
			workers.submit (() -> {
				if ( throwable == null ) { respond (s, base, request, response, value); }
				else { responder.respond (s, base, request, response, throwable.getCause ()); }
				base.getAsyncContext ().complete ();
				return null;
			});
		});

		base.startAsync ();
		base.setHandled (true);
	}

	public void respond (String s, org.eclipse.jetty.server.Request base, HttpServletRequest request, HttpServletResponse response, Object value)
	throws IOException, ServletException {
		int status = response.getStatus () == 0 ? HttpServletResponse.SC_OK : response.getStatus ();
		response.setStatus (status);
		mapper.writeValue (response.getOutputStream (), value);
		response.getOutputStream ().flush ();
	}

}
