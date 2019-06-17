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

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class HttpAsyncHandler extends AbstractHandler implements Consumer<ApiRouter<Request, CompletableFuture<Object>>> {

	private final ExecutorService workers = Executors.newCachedThreadPool ();
	private final ObjectMapper mapper;
	private final ExceptionResponder responder;
	private final long timeout = TimeUnit.SECONDS.toMillis (30);
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

		this.responder = responder == null ? new ExceptionResponder (this.mapper) : responder;
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

		String [] parts = API.saveSplitParts (base.getHttpURI ().getPath ());
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
		base.getAsyncContext ().setTimeout (timeout);
		base.getAsyncContext ().addListener (new TimeoutListener (base, future));
		base.setHandled (true);
	}

	public void respond (String s, org.eclipse.jetty.server.Request base, HttpServletRequest request, HttpServletResponse response, Object value)
	throws IOException, ServletException {
		int status = response.getStatus () == 0 ? HttpServletResponse.SC_OK : response.getStatus ();
		response.setStatus (status);
		mapper.writeValue (response.getOutputStream (), value);
		response.getOutputStream ().flush ();
	}

	private static class TimeoutListener implements AsyncListener {
		private final CompletableFuture<Object> future;
		private final org.eclipse.jetty.server.Request request;

		public TimeoutListener (org.eclipse.jetty.server.Request request, CompletableFuture<Object> future) {
			this.future = future;
			this.request = request;
		}

		@Override public void onStartAsync (AsyncEvent event) throws IOException {}
		@Override public void onComplete (AsyncEvent event) throws IOException {}

		@Override public void onTimeout (AsyncEvent event) throws IOException {
			if ( ! future.isDone () ) {
				future.cancel (true);
			}
		}

		@Override public void onError (AsyncEvent event) throws IOException {
			if ( ! future.isDone () ) {
				future.completeExceptionally (event.getThrowable ());
			}
		}
	}

}
