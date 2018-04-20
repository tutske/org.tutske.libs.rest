package org.tutske.rest.internals;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.intThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.eclipse.jetty.server.Handler;
import org.junit.Before;
import org.junit.Test;
import org.tutske.rest.ControllerFunction;
import org.tutske.rest.HttpRequest;
import org.tutske.rest.routes.ControllerRoute;
import org.tutske.rest.UrlRouter;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.exceptions.ResponseException;
import org.tutske.rest.util.RoundTrip;


public class RestHandlerTest {

	private final UrlRouter<ControllerFunction> router = new UrlRouter<ControllerFunction> ().add (
		new ControllerRoute ("dummy", "/dummy", RestHandlerTest::dummy),
		new ControllerRoute ("fail", "/fail", RestHandlerTest::fail),
		new ControllerRoute ("hard-fail", "/hard-fail", RestHandlerTest::hardFail)
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
		System.out.println ("Failing hard");
		throw new RuntimeException ("Internal Error");
	}

	private RoundTrip trip = new RoundTrip ();
	private RestHandler handler;

	@Before
	public void setup () throws Exception {
		trip.setup ();
	}

	@Test
	public void sanity_check_directly_writing_to_output () throws Exception {
		trip._output.write ("Hello World!".getBytes ());
		assertThat (trip.output (), is ("Hello World!"));
	}

	@Test
	public void sanity_check_output_functionality () throws Exception {
		trip.response.getWriter ().write ("Hello World!");
		assertThat (trip.output (), is ("Hello World!"));
	}

	@Test
	public void it_should_return_a_success_200_ok_code_for_normal_actions () throws Exception {
		handler = new RestHandler (router);
		trip.get (handler, "/dummy");
		verify (trip.response).setStatus (200);
	}

	@Test
	public void it_should_give_gson_output_as_specified_in_the_response () throws Exception {
		handler = new RestHandler (router, new ContentSerializer ("default") {{
			put ("default", new JsonSerializer ());
		}});

		trip.get (handler, "/dummy");

		assertThat (trip.output (), containsString ("greeting"));
		assertThat (trip.output (), containsString ("Hello World"));
	}

	@Test
	public void it_should_do_nothing_when_the_url_is_not_routed () throws Exception {
		handler = new RestHandler (router);
		trip.get (handler, "/unknown/path/to/resources");
		verify (trip.response, never ()).setStatus (anyInt ());
	}

	@Test
	public void it_should_return_an_error_code_when_a_response_exception_is_thrown () throws Exception {
		Handler handler = new ErrorAwareHandlerList (new RestHandler (router));
		handler.start ();
		trip.get (handler, "/fail");
		verify (trip.response).setStatus (codeRange (400));
	}

	@Test
	public void it_should_return_an_error_code_when_a_different_exception_is_thrown () throws Exception {
		Handler handler = new ErrorAwareHandlerList (new RestHandler (router));
		handler.start ();
		trip.get (handler, "/hard-fail");
		verify (trip.response).setStatus (codeRange (500));
	}

	@Test
	public void it_should_proceed_as_normal_when_not_having_any_filters () throws Exception {
		handler = new RestHandler (router);
		trip.get (handler, "/dummy");
		verify (trip.response).setStatus (200);
	}

	@Test
	public void it_should_apply_a_filter_that_matches () throws Exception {
		handler = new RestHandler (router);
		trip.get (handler, "/dummy");
	}

	@Test
	public void it_should_keep_the_status_set_from_the_handle () throws Exception {
		router.add (new ControllerRoute ("set-status", "/set-status-code", (request) -> {
			when (trip.response.getStatus ()).thenReturn (201);
			request.getServletResponse ().setStatus (201);
			return new RestObject ();
		}));

		handler = new RestHandler (router);
		trip.get (handler, "/set-status-code");

		verify (trip.response, times (0)).setStatus (intThat (not (201)));
		verify (trip.response, atLeast (1)).setStatus (201);
	}

	private Integer codeRange (int lower) {
		return intThat (allOf (
			greaterThanOrEqualTo (lower),
			lessThan (lower + 100)
		));
	}

}
