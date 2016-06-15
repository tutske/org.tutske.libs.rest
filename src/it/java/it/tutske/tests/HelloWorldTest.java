package it.tutske.tests;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;


public class HelloWorldTest {

	private TestUtils.Application application;
	private HttpClient client;

	@Before
	public void setup () throws Exception {
		application = TestUtils.getApplication ();
		application.start ();
		client = new HttpClient ();
		client.start ();
	}

	@After
	public void teardown () throws Exception {
		application.stop ();
	}

	@Test
	public void it_should_say_hello () throws Exception {
		URI uri = TestUtils.getUrl ("/hello");
		String content = client.GET (uri).getContentAsString ();
		System.out.println (content);
		assertThat (content, containsString ("greeting"));
	}

	@Test
	public void it_should_say_hello_on_posts () throws Exception {
		URI uri = TestUtils.getUrl ("/hello");
		String content = client.POST (uri).send ().getContentAsString ();
		System.out.println (content);
		assertThat (content, containsString ("type"));
		assertThat (content, containsString ("title"));
		assertThat (content, containsString ("detail"));
	}

	@Test
	public void it_should_parse_the_path_params () throws Exception {
		URI uri = TestUtils.getUrl ("/file/filename.txt");
		String content = client.GET (uri).getContentAsString ();
		System.out.println (content);
		assertThat (content, containsString ("filename.txt"));
	}

	@Test
	public void it_should_not_accept_delete_requests () throws Exception {
		URI uri = TestUtils.getUrl ("/hello");
		client.newRequest (uri).method (HttpMethod.DELETE).send ().getContentAsString ();
	}

}
