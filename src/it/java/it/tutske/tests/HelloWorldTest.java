package it.tutske.tests;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MetaData;
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
	public void it_should_return_xml_when_asked () throws Exception {
		URI uri = TestUtils.getUrl ("/hello");
		String content = client.newRequest (uri)
			.method (HttpMethod.GET)
			.header ("Accept", "application/xml")
			.send ().getContentAsString ();

		System.out.println (content);

		assertThat (content, containsString ("<?xml "));
	}

	@Test
	public void it_should_return_xml_when_failing_on_the_server () throws Exception {
		URI uri = TestUtils.getUrl ("/hello");
		String content = client.newRequest (uri)
			.method (HttpMethod.POST)
			.header ("Accept", "application/xml")
			.send ().getContentAsString ();

		System.out.println (content);

		assertThat (content, containsString ("<?xml "));
	}

	@Test
	public void it_should_not_accept_delete_requests () throws Exception {
		URI uri = TestUtils.getUrl ("/hello");
		String content = client.newRequest (uri).method (HttpMethod.DELETE).send ().getContentAsString ();

		System.out.println (content);

		assertThat (content, containsString ("Not Found"));
	}

	@Test
	public void it_should_say_non_existing_url_is_not_found () throws Exception {
		URI uri = TestUtils.getUrl ("/path_does_not_exist");
		ContentResponse response = client.GET (uri);
		String content = response.getContentAsString ();

		System.out.println (content);

		assertThat (response.getStatus (), is (404));
		assertThat (content, containsString ("Not Found"));
	}

	@Test
	public void it_should_say_non_existing_url_is_not_found_in_xml () throws Exception {
		URI uri = TestUtils.getUrl ("/path_does_not_exist");
		ContentResponse response = client.newRequest (uri)
			.method (HttpMethod.GET)
			.header ("Accept", "application/xml")
			.send ();
		String content = response.getContentAsString ();

		System.out.println (content);

		assertThat (content, containsString ("<?xml"));
	}

}
