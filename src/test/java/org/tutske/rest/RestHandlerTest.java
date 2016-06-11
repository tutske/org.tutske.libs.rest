package org.tutske.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.intThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.eclipse.jetty.server.Request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.tutske.rest.UrlRoute.ControllerRoute;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.exceptions.ResponseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;


public class RestHandlerTest {

	private static final Gson gson = new GsonBuilder ().create ();
	private static final UrlRouter<ControllerFunction> router = new UrlRouter<ControllerFunction> ().add (
		new ControllerRoute ("dummy", "/dummy", RestHandlerTest::dummy),
		new ControllerRoute ("fail", "/fail", RestHandlerTest::fail),
		new ControllerRoute ("fail", "/hard-fail", RestHandlerTest::hardFail)
	);

	private static RestObject dummy (HttpRequest request) {
		return new RestObject () {{
			v ("greeting", "Hello World!");
		}};
	}

	private static RestObject fail (HttpRequest request) {
		throw new ResponseException ("Wrong Request");
	}

	private static RestObject hardFail (HttpRequest request) {
		throw new RuntimeException ("Internal Error");
	}

	private Request base;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private RestHandler handler;

	private ByteArrayOutputStream _output;
	private PrintWriter _writer;

	@Before
	public void setup () throws Exception {
		base = mock (Request.class);
		request = mock (HttpServletRequest.class);
		response = mock (HttpServletResponse.class);

		_output = new ByteArrayOutputStream ();
		_writer = new PrintWriter (_output);
		when (response.getWriter ()).thenReturn (_writer);
	}

	@Test
	public void sanity_check_directly_writing_to_output () throws Exception {
		_output.write ("Hello World!".getBytes ());
		assertThat (output (), is ("Hello World!"));
	}

	@Test
	public void sanity_check_output_functionality () throws Exception {
		response.getWriter ().write ("Hello World!");
		assertThat (output (), is ("Hello World!"));
	}

	@Test
	public void it_should_return_a_success_200_ok_code_for_normal_actions () throws Exception {
		handler = new RestHandler (router, gson);
		get ("/dummy");
		verify (response).setStatus (200);
	}

	@Test
	public void it_should_give_gson_output_as_specified_in_the_response () throws Exception {
		handler = new RestHandler (router, gson);
		get ("/dummy");
		assertThat (output (), containsString ("greeting"));
		assertThat (output (), containsString ("Hello World"));
	}

	@Test
	public void it_should_do_nothing_when_the_url_is_not_routed () throws Exception {
		handler = new RestHandler (router, gson);
		get ("/unknown/path/to/resources");
		verify (response, never ()).setStatus (anyInt ());
	}

	@Test
	public void it_should_return_an_error_code_when_a_response_exception_is_thrown () throws Exception {
		handler = new RestHandler (router, gson);
		get ("/fail");
		verify (response).setStatus (codeRange (400));
	}

	@Test
	public void it_should_return_an_error_code_when_a_different_exception_is_thrown () throws Exception {
		handler = new RestHandler (router, gson);
		get ("/hard-fail");
		verify (response).setStatus (codeRange (500));
	}

	@Test
	public void it_should_proceed_as_normal_when_not_having_any_filters () throws Exception {
		handler = new RestHandler (router, gson);
		get ("/dummy");
		verify (response).setStatus (200);
	}

	@Test
	public void it_should_apply_a_filter_that_matches () throws Exception {
		handler = new RestHandler (router, gson);
		get ("/dummy");
	}

	private void get (String url) throws Exception {
		when (request.getMethod ()).thenReturn ("GET");
		handler.handle (url, base, request, response);
	}

	private String output () {
		_writer.flush ();
		return new String (_output.toByteArray ());
	}

	private Integer codeRange (int lower) {
		return intThat (allOf (
			greaterThanOrEqualTo (lower),
			lessThan (lower + 100)
		));
	}

}
