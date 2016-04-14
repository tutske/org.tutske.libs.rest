package it.tutske.tests;

import it.tutske.util.CommandRunner;
import it.tutske.util.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class HelloWorldTest {

	private static final int PORT = 18080;
	private Process server;

	@Before
	public void setup () throws Exception {
		Environment env = new Environment ().with ("TS_WEBSOCKET_PORT", "" + PORT);
		server = CommandRunner.runJar (env);
		Thread.sleep (600);
	}

	@After
	public void teardown () throws InterruptedException {
		server.destroy ();
	}

	@Test
	public void it_should_say_hello () throws Exception {
		String content = request ("/hello");
		System.out.println (content);
	}

	@Test
	public void it_should_say_hello_on_posts () throws Exception {
		System.out.println (request ("POST", "/hello"));
	}

	private String request (String method, String path) throws IOException {
		StringBuilder builder = new StringBuilder ();

		HttpURLConnection connection = (HttpURLConnection) new URL ("http", "localhost", PORT, path).openConnection ();
		connection.setRequestMethod (method);

		connection.connect ();
		Scanner scanner = new Scanner (connection.getInputStream ());
		while ( scanner.hasNextLine () ) {
			builder.append (scanner.nextLine ()).append ("\n");
		}

		return builder.toString ();
	}

	private String request (String path) throws IOException {
		return request ("GET", path);
	}
}
