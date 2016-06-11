package org.tutske.rest.internals;

import org.tutske.rest.ThrowingFunction;

import java.util.LinkedList;
import java.util.List;


public class Chain<S, T> {

	private int current = 0;
	private int depth = 0;
	private List<Filter<S, T>> routes;
	private ThrowingFunction<S, T> destination;

	public Chain (ThrowingFunction<S, T> destination) {
		this (destination, new LinkedList<> ());
	}

	public Chain (ThrowingFunction<S, T> destination, List<Filter<S, T>> routes) {
		this.destination = destination;
		this.routes = routes;
	}

	public T call (S source) throws Exception {
		if ( current > depth ) {
			throw new RuntimeException ("Invalid call of filter chain");
		}

		int index = current;

		current++;
		depth++;

		T result;
		if ( index == routes.size ()) {
			result = destination.apply (source);
		} else {
			result = this.routes.get (index).call (source, this);
		}

		depth--;

		return result;
	}

}
