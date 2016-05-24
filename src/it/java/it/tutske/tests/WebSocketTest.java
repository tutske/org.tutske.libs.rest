package it.tutske.tests;

import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static it.tutske.controllers.JavaSocket.*;


public class WebSocketTest {

	private TestUtils.Application application;

	@Before
	public void setup () throws Exception {
		application = TestUtils.getApplication ();
		application.start ();
	}

	@After
	public void teardown () throws Exception {
		application.stop ();
	}

	public static class SimpleEchoSocket extends WebSocketAdapter {

		private final int MAX_BUFF_SIZE = 150 * 1000 * 1000; /* 150 mb */
		private final CountDownLatch closeLatch;

		@SuppressWarnings ("unused")
		private Session session;

		public SimpleEchoSocket () {
			this.closeLatch = new CountDownLatch (1);
		}

		public boolean awaitClose (int duration, TimeUnit unit) throws InterruptedException {
			return this.closeLatch.await (duration, unit);
		}

		@Override
		public void onWebSocketClose (int statusCode, String reason) {
			System.out.printf ("Connection closed: %d - %s%n", statusCode, reason);
			this.session = null;
			this.closeLatch.countDown ();
		}

		@Override
		public void onWebSocketConnect (Session session) {
			System.out.printf ("Got connect: %s%n", session);
			session.getPolicy ().setMaxBinaryMessageSize (MAX_BUFF_SIZE);
			this.session = session;
			try {
				Future<Void> fut;
				fut = session.getRemote ().sendStringByFuture ("Hello");
				fut.get (2, TimeUnit.SECONDS);
				fut = session.getRemote ().sendStringByFuture ("Thanks for the conversation.");
				fut.get (2, TimeUnit.SECONDS);
				session.close (StatusCode.NORMAL, "I'm done");
			} catch ( Throwable t ) {
				t.printStackTrace ();
			}
		}

		@Override
		public void onWebSocketBinary (byte [] bytes, int start, int end) {
			try	{
				System.out.println ("Got a bytes message of length: " + start + " - " + end + " (" + bytes.length + ")");
				ObjectInputStream objects = new ObjectInputStream (new ByteArrayInputStream (bytes, start, end));
				showComunication (objects.readObject ());
			} catch ( ClassNotFoundException e ) {
				e.printStackTrace ();
			} catch ( IOException e ) {
				getSession ().close ();
			}
		}

		private void showComunication (Object object) {
			if ( ! (object instanceof ComunicationEvent) ) {
				System.out.println ("We got something that was not a communication event");
				return;
			}
			ComunicationEvent event = (ComunicationEvent) object;

			String msg = event.message.substring (0, Math.min (event.message.length (), 100));
			msg += event.message.length () > 100 ? " ..." : "";

			System.out.println ();
			System.out.println ("> type    : " + event.type);
			System.out.println ("> title   : " + event.title);
			System.out.println ("> date    : " + event.date);
			System.out.println ("> content : " + msg);
			System.out.println ();
		}

	}

	@Test
	public void it_should_connect_two_clients_over_a_socket () throws Exception {
		URI uri = TestUtils.getSocketUri ("/java");

		WebSocketClient client = new WebSocketClient ();
		SimpleEchoSocket socket = new SimpleEchoSocket ();

		client.start ();
		ClientUpgradeRequest request = new ClientUpgradeRequest ();
		client.connect (socket, uri, request);
		System.out.printf ("Connecting to : %s%n", uri);

		socket.awaitClose (5, TimeUnit.SECONDS);
	}

}
