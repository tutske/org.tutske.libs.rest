package org.tutske.rest.internals;

import org.tutske.rest.Filter;
import org.tutske.rest.HttpRequest.Method;
import org.tutske.rest.ThrowingFunction;
import org.tutske.rest.UrlRoute;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class FilterCollection<S, T> {

	private List<UrlRoute<Filter<S, T>>> routes = new LinkedList<> ();

	public Chain<S, T> createChain (String url, ThrowingFunction<S, T> destination) {
		List<Filter<S, T>> filters = new LinkedList<> ();
		String [] parts = url.substring (1).split ("/");
		for ( UrlRoute<Filter<S, T>> route : routes ) {
			String id = route.toId (Method.GET, url, parts);
			if ( id != null ) { filters.add (route.getHandler (id)); }
		}
		return new Chain<> (destination, filters);
	}

	public FilterCollection<S, T> add (String label, UrlRoute<Filter<S, T>> ... routes) {
		Collections.addAll (this.routes, routes);
		return this;
	}

	public FilterCollection<S, T> add (UrlRoute<Filter<S, T>> ... routes) {
		Collections.addAll (this.routes, routes);
		return this;
	}

}
