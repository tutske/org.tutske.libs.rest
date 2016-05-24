package it.tutske.controllers;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tutske.rest.SocketRequest;
import org.tutske.rest.WebSocketListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;


public class JavaSocket extends WebSocketAdapter implements WebSocketListener {

	private static final Logger logger = LoggerFactory.getLogger (JavaSocket.class);

	public static WebSocketListener create (SocketRequest request) {
		return new JavaSocket ();
	}

	public static class ComunicationEvent implements Serializable {
		public final Date date = new Date ();
		public final String type;
		public final String title;
		public final String message;
		public ComunicationEvent (String type, String title, String message) {
			this.type = type;
			this.title = title;
			this.message = message;
		}
	}

	@Override
	public void onWebSocketClose (int statusCode, String reason) {
		super.onWebSocketClose (statusCode, reason);
	}

	@Override
	public void onWebSocketConnect (Session session) {
		super.onWebSocketConnect (session);
		try {
			String content = "This is the content of the communication";
			System.out.println ("nr of bytes: " + content.getBytes ("UTF-8").length);
			send (session, new ComunicationEvent ("type:something", "First Section", content));
		} catch ( IOException exception ) {
			logger.debug (exception.getMessage (), exception);
			session.close ();
		}
	}

	@Override
	public void onWebSocketError (Throwable throwable) {
		super.onWebSocketError (throwable);
		logger.debug (throwable.getMessage (), throwable);
	}

	@Override
	public void onWebSocketBinary (byte [] bytes, int i, int i1) {
	}

	@Override
	public void onWebSocketText (String message) {
	}

	private void send (Session session, Serializable subject) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream ();
		new ObjectOutputStream (stream).writeObject (subject);
		byte [] bytes = stream.toByteArray ();

		int max = 65536;
		int start = 0;
		while ( bytes.length - start > max ) {
			session.getRemote ().sendPartialBytes (ByteBuffer.wrap (bytes, start, max), false);
			start += max;
		}
		session.getRemote ().sendPartialBytes (ByteBuffer.wrap (bytes, start, bytes.length - start), true);
	}

	private String longContent () {
		StringBuilder builder = new StringBuilder ();
		String base = "This is a long single line of 100 characters that will be repeated over and over again ... and again";
		int max = 1000000;

		for ( int i = 0; i < max; i++ ) {
			builder.append (i).append (base).append ("\n");
		}

		return builder.toString ();
	}

}
