package it.tutske.controllers;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.tutske.rest.SocketRequest;
import org.tutske.rest.WebSocketListener;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ChatSocket extends WebSocketAdapter implements WebSocketListener {

	private static final Map<String, ConcurrentLinkedQueue<Session>> SESSIONS = new ConcurrentHashMap<> ();

	public static WebSocketListener create (SocketRequest request) {
		String room = request.pathParams ().get ("room");
		String user = request.queryParams ().get ("username");

		if ( ! SESSIONS.containsKey (room) ) {
			SESSIONS.put (room, new ConcurrentLinkedQueue<> ());
		}

		return new ChatSocket (SESSIONS.get (room), room, user);
	}

	private final Collection<Session> sessions;
	private final String room;
	private final String username;

	public ChatSocket (Collection<Session> sessions, String room, String username) {
		if ( room == null || username == null ) {
			throw new NullPointerException ();
		}
		this.sessions = sessions;
		this.room = room;
		this.username = username;
	}

	@Override
	public void onWebSocketClose (int statusCode, String reason) {
		System.out.println (String.format ("user left (room %s): %s because %s", room, username, reason));
		sessions.remove (getSession ());
		super.onWebSocketClose (statusCode, reason);
	}

	@Override
	public void onWebSocketConnect (Session session) {
		super.onWebSocketConnect (session);
		sessions.add (session);
		send (session, "Hello from room: " + room);
		System.out.println (String.format ("new user joined (room %s): %s", room, username));
	}

	@Override
	public void onWebSocketError (Throwable throwable) {
		System.out.println (String.format ("Socket Error (%s): %s", room, throwable.getMessage ()));
		throwable.printStackTrace ();
	}

	@Override
	public void onWebSocketBinary (byte [] bytes, int i, int i1) {
	}

	@Override
	public void onWebSocketText (String message) {
		System.out.println (String.format ("(room %s) %s: %s", room, username, message));
		String msg = username + ": " + message;
		for ( Session session : sessions ) {
			if ( ! session.equals (getSession ()) ) {
				send (session, msg);
			}
		}

		if ( "close all".equals (message) ) {
			sessions.forEach (Session::close);
			sessions.clear ();
		}
	}

	private void send (Session session, String message) {
		if ( ! session.isOpen () ) { return; }
		try {
			session.getRemote ().sendString (message);
		} catch ( IOException e ) {
			e.printStackTrace ();
			session.close ();
		}
	}

}
