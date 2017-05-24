package org.tutske.rest.internals;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.tutske.rest.ControllerFunction;
import org.tutske.rest.Filter;
import org.tutske.rest.HttpRequest;
import org.tutske.rest.ThrowingFunction;
import org.tutske.rest.UrlRoute.SimpleRoute;
import org.tutske.rest.data.RestObject;


public class RestFilterTest {

	FilterCollection<HttpRequest, RestObject> filters = new FilterCollection<> ();
	ThrowingFunction<HttpRequest, RestObject> destination = mock (ThrowingFunction.class);

	Filter<HttpRequest, RestObject> passThrough = (request, chain) -> chain.call (request);
	Filter<HttpRequest, RestObject> shortCircuit = (request, chain) -> null;

	@Test
	public void it_should_create_a_chain_that_calls_the_destination () throws Exception {
		Chain chain = filters.createChain ("/path/to/resource", destination);
		chain.call (mock (HttpRequest.class));
		verify (destination).apply (any ());
	}

	@Test
	public void it_should_return_a_response_based_on_the_response_from_the_destinaton () throws Exception {
		ThrowingFunction<HttpRequest, RestObject> destination = (request) -> new RestObject () {{
			v ("key", "value");
		}};

		Chain<HttpRequest, RestObject> chain = filters.createChain ("/path/to/resource", destination);
		RestObject result = chain.call (mock (HttpRequest.class));

		assertThat (result.get ("key"), is ("value"));
	}

	@Test
	public void it_should_return_a_response_based_on_the_response_from_the_destinaton_when_filters_pass_through ()
	throws Exception {
		ThrowingFunction<HttpRequest, RestObject> destination = (request) -> new RestObject () {{
			v ("key", "value");
		}};

		filters.add (
			new SimpleRoute<> ("resource", "/path/to/resource", passThrough),
			new SimpleRoute<> ("resource", "/path/to/resource", passThrough)
		);

		Chain<HttpRequest, RestObject> chain = filters.createChain ("/path/to/resource", destination);
		RestObject result = chain.call (mock (HttpRequest.class));

		assertThat (result.get ("key"), is ("value"));
	}

	@Test
	public void it_should_return_the_modified_reponse_when_filters_perform_modifcations () throws Exception {
		ThrowingFunction<HttpRequest, RestObject> destination = (request) -> new RestObject () {{
			v ("key", "value");
		}};
		Filter<HttpRequest, RestObject> modifier = (request, chain) -> {
			RestObject result = chain.call (request);
			result.put ("key", "modified value");
			return result;
		};

		filters.add (new SimpleRoute<> ("resource", "/path/to/resource", modifier));
		Chain<HttpRequest, RestObject> chain = filters.createChain ("/path/to/resource", destination);
		RestObject result = chain.call (mock (HttpRequest.class));

		assertThat (result.get ("key"), is ("modified value"));
	}

	@Test
	public void it_should_apply_filters_that_match_the_url_path () throws Exception {
		Filter<HttpRequest, RestObject> filter = mock (Filter.class);

		filters.add (
			"dummy filters",
			new SimpleRoute<> ("dummy", "/dummy", filter)
		);

		Chain chain = filters.createChain ("/dummy", destination);
		chain.call (mock (HttpRequest.class));

		verify (filter).call (any (), any ());
	}

	@Test
	public void it_should_not_call_destination_when_filter_short_circuits () throws Exception {
		filters.add (
			new SimpleRoute<> ("short circuit", "/short", shortCircuit)
		);

		filters.createChain ("/short", destination).call (mock (HttpRequest.class));

		verify (destination, never ()).apply (any ());
	}

	@Test
	public void it_should_call_the_destination_if_all_filters_pass_through () throws Exception {
		filters.add (
			new SimpleRoute<> ("short circuit", "/short", passThrough)
		);

		filters.createChain ("/dummy", destination).call (mock (HttpRequest.class));

		verify (destination).apply (any ());
	}

	@Test
	public void it_should_call_the_last_filter_of_a_filter_chain () throws Exception {
		Filter<HttpRequest, RestObject> last = mock (Filter.class);

		filters.add (
			new SimpleRoute<> ("first", "/dummy", passThrough),
			new SimpleRoute<> ("second", "/dummy", passThrough),
			new SimpleRoute<> ("third", "/dummy", last)
		);

		filters.createChain ("/dummy", destination).call (mock (HttpRequest.class));

		verify (last).call (any (), any ());
	}

	@Test
	public void it_should_call_the_first_filter_of_a_filter_chain () throws Exception {
		Filter<HttpRequest, RestObject> first = mock (Filter.class);

		filters.add (
			new SimpleRoute<> ("first", "/dummy", first),
			new SimpleRoute<> ("second", "/dummy", passThrough),
			new SimpleRoute<> ("third", "/third", passThrough)
		);

		filters.createChain ("/dummy", destination).call (mock (HttpRequest.class));

		verify (first).call (any (), any ());
	}

	@Test
	public void it_should_not_call_subsequent_filters_when_one_short_circuits () throws Exception {
		Filter<HttpRequest, RestObject> filter = mock (Filter.class);

		filters.add (
			new SimpleRoute<> ("first", "/dummy", passThrough),
			new SimpleRoute<> ("second", "/dummy", passThrough),
			new SimpleRoute<> ("second", "/dummy", shortCircuit),
			new SimpleRoute<> ("third", "/dummy", passThrough),
			new SimpleRoute<> ("forth", "/dummy", filter)
		);

		filters.createChain ("/dummy", destination).call (mock (HttpRequest.class));

		verify (filter, never ()).call (any (), any ());
	}

	@Test
	public void it_should_only_call_filters_that_match_the_path () throws Exception {
		Filter<HttpRequest, RestObject> filter = mock (Filter.class);

		filters.add (
			new SimpleRoute<> ("dummy", "/dummy", passThrough),
			new SimpleRoute<> ("not_matched_route", "/not_matched_route", filter)
		);

		filters.createChain ("/dummy", destination).call (mock (HttpRequest.class));

		verify (filter, never ()).call (any (), any ());
	}

}
