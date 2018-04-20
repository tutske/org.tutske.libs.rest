package org.tutske.rest;

import static org.tutske.rest.HttpRequest.Method;

import org.tutske.rest.routes.UrlRoute;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class UrlRouter<T> {

	private final List<UrlRoute<T>> routes = new LinkedList<UrlRoute<T>> ();
	private final Map<String, UrlRoute<T>> names = new HashMap<String, UrlRoute<T>> ();

	/**
	 * Just some syntax to have a nicer way to group routes when adding them to the
	 * router.
	 *
	 * ```java
	 * new UrlRouter.add (
	 *     "basic routes",
	 *     new SimpleRoute (...),
	 *     new SimpleRoute (...),
	 * ).add (
	 *     "admin routes",
	 *     new SimpleRoute (...),
	 *     new SimpleRoute (...),
	 * );
	 * ```
	 *
	 * @param section
	 * @param routes
	 * @return
	 */
	public UrlRouter<T> add (String section, UrlRoute<T> ... routes) {
		return add (routes);
	}

	public UrlRouter<T> add (UrlRoute<T> ... routes) {
		for ( UrlRoute<T> route : routes ) {
			this.routes.add (route);
			route.getIdentifiers ().forEach (id -> {
				if ( this.names.containsKey (id) ) {
					String msg = "There is a route registered with the sane name: " + id;
					throw new RuntimeException (msg);
				}
				this.names.put (id, route);
			});
		}
		return this;
	}

	public String route (Method method, String url) {
		String [] parts = url.substring (1).split ("/");
		for ( UrlRoute<T> route : routes ) {
			String id = route.toId (method, url, parts);
			if ( id != null ) { return id; }
		}
		return null;
	}

	public UrlRoute<T> find (String name) {
		return names.get (name);
	}

}
