package org.tutske.rest;

import static org.tutske.rest.HttpRequest.Method;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


/**
 * The simple route matching will work for urls that only have exact matching parts or a
 * variable part. Things such as `/fixed/part/:variable/or/:other/variables`. Simple route
 * matching should be very fast, if the split string is passed instead of the plain url
 * this should not have a noticble impact on performance.
 *
 * have a separate class that can do more fancy matchings such as globbing more than
 * one part of the url. This could still be done with a bit more fancy matching
 * algorithm.
 *
 * Have a separate class that can do plain old regex matchings.
 *
 * Advice people to use the first type of routes whenever they can, and to put the
 * more expensive routes towards the end. unless they are used very often.
 *
 * When designing the routes and the application, you can do further processing of
 * the url within the controller. Not all possible scenarios have to be handed by the
 * urlrouter.
 */
public abstract class UrlRoute {

	private abstract static class BaseRoute extends UrlRoute {
		protected final String identifier;
		protected final ControllerFunction function;
		protected final EnumSet<Method> methods;
		protected final String [] descriptor;
		protected final boolean [] shouldMatch;

		public BaseRoute (String identifier, String descriptor, ControllerFunction function) {
			this (identifier, descriptor, EnumSet.of (Method.GET), function);
		}

		public BaseRoute (String identifier, String descriptor, EnumSet<Method> methods, ControllerFunction function) {
			if ( ! descriptor.startsWith ("/") ) {
				throw new RuntimeException ("invalid descriptor: " + descriptor);
			}

			this.identifier = identifier;
			this.function = function;
			this.methods = methods;
			this.descriptor = descriptor.substring (1).split ("/");
			this.shouldMatch = new boolean [this.descriptor.length];

			processDescriptor ();
		}

		@Override public String getIdentifier () {
			return identifier;
		}

		@Override public ControllerFunction getHandler () {
			return function;
		}

		@Override public boolean matches (Method method, String url) {
			return matches (method, url, url.substring (1).split ("/"));
		}

		@Override public String linkTo (Map<String, String> params) {
			String [] constructed = new String [descriptor.length];
			for ( int i = 0; i < descriptor.length; i++ ) {
				String part = descriptor[i];
				constructed[i] = part.startsWith (":") ? params.get (part.substring (1)) : part;
			}
			return "/" + String.join ("/", constructed);
		}

		private void processDescriptor () {
			for ( int i = 0; i < descriptor.length; i++ ) {
				this.shouldMatch[i] = ! descriptor[i].startsWith (":");
			}
		}
	}

	public static class RootRoute extends BaseRoute {
		public RootRoute (ControllerFunction function) {
			super ("root", "ROOT", function);
		}

		@Override public boolean matches (Method method, String url, String [] parts) {
			return parts.length == 1 && "".equals (parts[1]);
		}

		@Override public Map<String, String> extractMatches (String url, String [] parts) {
			return new HashMap<String, String> ();
		}
	}

	/**
	 * This will work for url that only have exact matching parts or a variable part.
	 * Things such as `/fixed/part/:variable/or/:other/variables`.
	 *
	 * basic route matching, should be very fast, if the split string is passed instead
	 * of the plain url this should not have a noticble impact on performance.
	 *
	 * have a separate class that can do more fancy matchings such as globbing more than
	 * one part of the url. This could still be done with a bit more fancy matching
	 * algorithm.
	 *
	 * Have a separate class that can do plain old regex matchings.
	 *
	 * advice people to use the first type of routes whenever they can, and to put the
	 * more expensive routes towards the end. unless they are used very often.
	 *
	 * when desinging the routes and the applicatino, you can do further processing of
	 * the url within the controller. Not all passible senarios have to be handed bo the
	 * urlrouter.
	 */
	public static class SimpleRoute extends BaseRoute {
		public SimpleRoute (String identifier, String descriptor, ControllerFunction function) {
			super (identifier, descriptor, function);
		}

		public SimpleRoute (String identifier, String descriptor, EnumSet<Method> methods, ControllerFunction function) {
			super (identifier, descriptor, methods, function);
		}

		@Override public boolean matches (Method method, String url, String [] parts) {
			if ( parts.length != this.descriptor.length || ! methods.contains (method) ) {
				return false;
			}
			for ( int i = 0; i < descriptor.length; i++ ) {
				if ( shouldMatch[i] && ! descriptor[i].equals (parts[i]) ) {
					return false;
				}
			}
			return true;
		}

		@Override public Map<String, String> extractMatches (String url, String [] parts) {
			Map<String, String> extracted = new HashMap<String, String> ();
			for ( int i = 0; i < descriptor.length; i++ ) {
				if ( shouldMatch [i] ) { continue; }
				extracted.put (descriptor[i].substring (1), parts[i]);
			}
			return extracted;
		}
	}

	abstract public String getIdentifier ();
	abstract public boolean matches (Method method, String url);
	abstract public boolean matches (Method method, String url, String [] parts);
	abstract public Map<String, String> extractMatches (String url, String [] parts);
	abstract public ControllerFunction getHandler ();
	abstract public String linkTo (Map<String, String> params);

}
