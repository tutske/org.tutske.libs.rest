package org.tutske.rest;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.tutske.rest.routes.UrlRoute;


public class Matchers {

	public static Matcher<UrlRoute<?>> routesTo (HttpRequest.Method method, String url) {
		return new RoutesToMatcher (method, url);
	}

	public static class RoutesToMatcher extends BaseMatcher<UrlRoute<?>> {
		private final HttpRequest.Method method;
		private final String url;

		public RoutesToMatcher (HttpRequest.Method method, String url) {
			this.method = method;
			this.url = url;
		}

		@Override public boolean matches (Object item) {
			if ( ! (item instanceof UrlRoute) ) { return false; }
			UrlRoute<?> route = (UrlRoute<?>) item;
			return route.toId (method, url, url.substring (1).split ("/")) != null;
		}

		@Override
		public void describeTo (Description description) {
			description.appendText ("a route that matches on the url (" + method + ") " + url);
		}
	}

}
