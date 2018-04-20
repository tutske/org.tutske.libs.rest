package org.tutske.rest.internals;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.tutske.rest.ControllerFunction;
import org.tutske.rest.HttpRequest;
import org.tutske.rest.RestFilterCollection;
import org.tutske.rest.UrlRoute;
import org.tutske.rest.UrlRoute.ControllerRoute;
import org.tutske.rest.UrlRouter;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.data.RestStructure;
import org.tutske.rest.UrlRoute.SimpleRoute;
import org.tutske.rest.util.RoundTrip;

import java.util.function.Consumer;


public class UrlFilterTest {

	private final ContentSerializer serializer = new ContentSerializer ("application/json") {{
		put ("application/json", new JsonSerializer ());
	}};
	private final UrlRouter<ControllerFunction> router = new UrlRouter<ControllerFunction> ().add (
		new ControllerRoute ("home", "/", UrlFilterTest::dummy),
		new ControllerRoute ("one", "/:one", UrlFilterTest::dummy),
		new ControllerRoute ("two", "/:one/:two", UrlFilterTest::dummy)
	);

	private final FilterCollection<HttpRequest, RestStructure> filters = new RestFilterCollection ();
	private final RestHandler handler = new RestHandler (router, filters, serializer);
	private final RoundTrip trip = new RoundTrip ();

	private static RestObject dummy (HttpRequest request) {
		return new RestObject () {{ v ("dummy", true); }};
	}

	@Before
	public void setup () throws Exception {
		trip.setup ();
	}

	@Test
	public void it_should_try_the_rules_in_the_filter () throws Exception {
		UrlRoute route = mock (UrlRoute.class);
		filters.add (route);

		trip.get (handler, "/");

		verify (route).toId (any (), any (), any ());
	}

	@Test
	public void it_should_call_the_methods_from_the_chain () throws Exception {
		Consumer<String> consumer = mock (Consumer.class);
		filters.add (new SimpleRoute<> ("root", "/", (r, c) -> {
			consumer.accept ("chained");
			return c.call (r);
		}));

		trip.get (handler, "/");

		verify (consumer).accept ("chained");
	}

	@Test
	public void it_should_not_call_methdos_that_dont_apply () throws Exception {
		filters.add (new SimpleRoute<> ("root", "/other", (r, c) -> {
			fail ("should not be called");
			return c.call (r);
		}));

		trip.get (handler, "/actual");
	}

	@Test
	public void it_should_not_call_any_filters_for_which_there_is_no_controller () throws Exception {
		String path = "/path/without/controller";
		filters.add (new SimpleRoute<> ("root", path, (r, c) ->  {
			fail ("should not be called");
			return c.call (r);
		}));

		trip.get (handler, path);
	}

	@Test
	public void it_should_call_multiple_methods_in_a_chain_with_same_route () throws Exception {
		Consumer<String> consumer = mock (Consumer.class);
		filters.add (new SimpleRoute<> ("root", "/", (r, c) -> {
			consumer.accept ("first");
			return c.call (r);
		}));
		filters.add (new SimpleRoute<> ("root", "/", (r, c) -> {
			consumer.accept ("second");
			return c.call (r);
		}));

		trip.get (handler, "/");

		verify (consumer).accept ("first");
		verify (consumer).accept ("second");
	}

	@Test
	public void it_should_calle_multiple_methods_in_a_chain_with_different_routes () throws Exception {
		Consumer<String> consumer = mock (Consumer.class);
		filters.add (new SimpleRoute<> ("root", "/:any", (r, c) -> {
			consumer.accept ("first");
			return c.call (r);
		}));
		filters.add (new SimpleRoute<> ("root", "/path", (r, c) -> {
			consumer.accept ("second");
			return c.call (r);
		}));

		trip.get (handler, "/path");

		verify (consumer).accept ("first");
		verify (consumer).accept ("second");
	}

}
