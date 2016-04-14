package org.tutske.rest;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebSocketListenerWrapper implements WebSocketListener {

	private static final Logger logger = LoggerFactory.getLogger (SocketHandler.class);
	private final WebSocketListener listener;

	public WebSocketListenerWrapper (WebSocketListener listener) {
		this.listener = listener;
	}

	@Override
	public void onWebSocketBinary (byte[] bytes, int i, int j) {
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
