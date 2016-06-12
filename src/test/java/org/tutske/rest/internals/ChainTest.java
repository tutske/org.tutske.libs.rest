package org.tutske.rest.internals;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.tutske.rest.Filter;
import org.tutske.rest.ThrowingFunction;

import java.util.Arrays;


public class ChainTest {

	ThrowingFunction<Object, Object> destination = (object) -> { return object; };
	Filter<Object, Object> shortCircuit = (object, chain) -> { return object; };
	Filter<Object, Object> doubleCall = (object, chain) -> {
		chain.call (object);
		return chain.call (object);
	};

	@Test
	public void it_should_call_the_filters_of_the_chain () throws Exception {
		Filter<Object, Object> filter = mock (Filter.class);

		Chain<Object, Object> chain = new Chain<> (destination, Arrays.asList (filter));
		chain.call (new Object ());

		verify (filter).call (any (), any (Chain.class));
	}

	@Test (expected = Exception.class)
	public void it_should_yell_when_a_step_calls_the_chain_twice () throws Exception {
		Chain<Object, Object> chain = new Chain<> (destination, Arrays.asList (doubleCall));
		chain.call (new Object ());
	}

	@Test
	public void it_should_not_call_down_to_other_filters_on_the_second_call () throws Exception {
		Filter<Object, Object> filter = mock (Filter.class);
		Chain<Object, Object> chain = new Chain<> (destination, Arrays.asList (doubleCall, filter));

		try { chain.call (new Object ()); }
		catch (Exception ignore) { }

		verify (filter, atMost (1)).call (any (), any ());
	}

	@Test (expected = Exception.class)
	public void it_should_fail_even_when_some_filter_short_circuits () throws Exception {
		Chain<Object, Object> chain = new Chain<> (destination, Arrays.asList (doubleCall, shortCircuit));
		chain.call (new Object ());
	}

}
