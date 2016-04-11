package org.tutske.rest;

import static org.tutske.rest.HttpRequest.Method;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class UrlRouter {

	private final List<UrlRoute> routes = new LinkedList<UrlRoute> ();
	private final Map<String, UrlRoute> names = new HashMap<String, UrlRoute> ();

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
	public UrlRouter add (String section, UrlRoute ... routes) {
		return add (routes);
	}

	public UrlRouter add (UrlRoute ... routes) {
		for ( UrlRoute route : routes ) {
			this.names.put (route.getIdentifier (), route);
			this.routes.add (route);
		}
		return this;
	}

	public UrlRoute route (Method method, String url) {
		String [] parts = url.substring (1).split ("/");
		for ( UrlRoute route : routes ) {
			if ( route.matches (method, url, parts) ) {
				return route;
			}
		}
		return null;
	}

	public UrlRoute find (String name) {
		return names.get (name);
	}

}
