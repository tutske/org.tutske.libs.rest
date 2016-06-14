package it.tutske.tests;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Scanner;


public class HelloWorldTest {

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

	@Test
	public void it_should_say_hello () throws Exception {
		String content = request ("/hello");
		assertThat (content, containsString ("greeting"));
	}

	@Test
	public void it_should_say_hello_on_posts () throws Exception {
		String content = request ("POST", "/hello");
		assertThat (content, containsString ("type"));
		assertThat (content, containsString ("title"));
		assertThat (content, containsString ("detail"));
	}

	@Test
	public void it_should_parse_the_path_params () throws IOException {
		String content = request ("GET", "/file/filename.txt");
		assertThat (content, containsString ("filename.txt"));
	}

	private String request (String method, String path) throws IOException {
		StringBuilder builder = new StringBuilder ();

		HttpURLConnection connection = (HttpURLConnection) TestUtils.getUrl (path).openConnection ();
		connection.setRequestMethod (method);

		connection.connect ();
		InputStream stream = (connection.getResponseCode () == 200) ? connection.getInputStream () : connection.getErrorStream ();
		Scanner scanner = new Scanner (stream);
		while ( scanner.hasNextLine () ) {
			builder.append (scanner.nextLine ()).append ("\n");
		}

		System.out.println (builder.toString ());
		return builder.toString ();
	}

	private String request (String path) throws IOException {
		return request ("GET", path);
	}
}
