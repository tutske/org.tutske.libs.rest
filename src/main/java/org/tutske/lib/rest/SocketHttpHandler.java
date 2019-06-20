package org.tutske.lib.rest;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tutske.lib.api.API;
import org.tutske.lib.api.ApiRouter;
import org.tutske.lib.api.Request;
import org.tutske.lib.utils.Bag;
import org.tutske.lib.utils.Exceptions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Consumer;


public class SocketHttpHandler extends WebSocketHandler implements Consumer<ApiRouter<Request, SocketHttpHandler.WebSocketListener>> {

	public static interface WebSocketListener extends org.eclipse.jetty.websocket.api.WebSocketListener {
	}

	@FunctionalInterface
	public static interface RequestSupplier {
		public Request get (ServletUpgradeRequest request, ServletUpgradeResponse response, Bag<String, String> path);
	}

	private static final Logger logger = LoggerFactory.getLogger (SocketHttpHandler.class);

	private final ThreadLocal<CreationInfo> info = new ThreadLocal<> ();
	private final ExceptionResponder responder;
	private final RequestSupplier requests;
	private ApiRouter<Request, SocketHttpHandler.WebSocketListener> router;

	public SocketHttpHandler () { this (null); }
	public SocketHttpHandler (RequestSupplier requests) {
		this.responder = new ExceptionResponder ();
		this.requests = requests != null ? requests : SocketRequest::new;
	}

	@Override
	public void accept (ApiRouter<Request, WebSocketListener> router) {
		if ( this.router == null ) { this.router = router; }
	}

	@Override
	public void handle (String s, org.eclipse.jetty.server.Request base, HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		String [] parts = API.saveSplitParts (base.getHttpURI ().getPath ());
		String identifier = router.toId (Request.Method.of (base.getMethod ()), "current", s, parts);

		if ( identifier == null ) { return; }

		this.info.set (new CreationInfo (identifier, s, parts, base, request, response));
		super.handle (s, base, request, response);
	}

	@Override
	public void configure (WebSocketServletFactory factory) {
		factory.setCreator ((request, response) -> {
			CreationInfo info = this.info.get ();
			try {
				Bag<String, String> query =router.extractMatches (info.identifier, info.s, info.parts);
				Request socketRequest = requests.get (request, response, query);
				return new WebSocketListenerWrapper (router.getHandler (info.identifier).apply (socketRequest));
			} catch (Throwable exception) {
				logger.debug ("creating web socket failed", exception);
				try { responder.respond (info.s, info.base, info.request, info.response, exception); }
				catch ( Exception e ) {
					logger.debug ("Failed to respond with exception", e);
					throw Exceptions.wrap (e);
				}
				throw exception;
			}
		});
	}


	private static class CreationInfo {
		private final String identifier;
		private final String s;
		private final String [] parts;
		private final org.eclipse.jetty.server.Request base;
		private final HttpServletRequest request;
		private final HttpServletResponse response;

		public CreationInfo (
			String identifier, String s, String [] parts, org.eclipse.jetty.server.Request base,
			HttpServletRequest request, HttpServletResponse response
		) {
			this.identifier = identifier;
			this.s = s;
			this.parts = parts;
			this.base = base;
			this.request = request;
			this.response = response;
		}
	}


	private static class WebSocketListenerWrapper implements WebSocketListener {
		private final WebSocketListener listener;

		public WebSocketListenerWrapper (WebSocketListener listener) {
			this.listener = listener;
		}

		@Override
		public void onWebSocketBinary (byte [] bytes, int i, int j) {
			try { listener.onWebSocketBinary (bytes, i, j); }
			catch ( Throwable throwable ) {
				logger.debug ("sending binary message to socket failed", throwable);
				throw throwable;
			}
		}

		@Override
		public void onWebSocketText (String s) {
			try { listener.onWebSocketText (s); }
			catch ( Throwable throwable ) {
				logger.debug ("sending text message to socket failed", throwable);
				throw throwable;
			}
		}

		@Override
		public void onWebSocketClose (int i, String s) {
			try { listener.onWebSocketClose (i, s); }
			catch ( Throwable throwable ) {
				logger.debug ("handling close on socket failed", throwable);
				throw throwable;
			}
		}

		@Override
		public void onWebSocketConnect (Session session) {
			try { listener.onWebSocketConnect (session); }
			catch ( Throwable throwable ) {
				logger.debug ("handling connect on socket failed", throwable);
				throw throwable;
			}
		}

		@Override
		public void onWebSocketError (Throwable throwable) {
			try { listener.onWebSocketError (throwable); }
			catch ( Throwable failure ) {
				logger.debug ("handling error on socket failed", failure);
				throw failure;
			}
		}
	}

}
