package org.tutske.rest;

import org.tutske.utils.Bag;

import static org.tutske.rest.HttpRequest.Method;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;


/**
 * The simple route matching will work for urls that only have exact matching parts or a
 * variable part. Things such as `/fixed/part/:variable/or/:other/variables`. Simple route
 * matching should be very fast, if the split string is passed instead of the plain url
 * this should not have a noticeable impact on performance.
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
public abstract class UrlRoute<T> {

	private abstract static class BaseRoute<T> extends UrlRoute<T> {
		protected final String identifier;
		protected final T handler;
		protected final EnumSet<Method> methods;
		protected final String [] descriptor;
		protected final boolean [] shouldMatch;
		protected final boolean allowTail;

		public BaseRoute (String identifier, String descriptor, T handler) {
			this (identifier, descriptor, EnumSet.of (Method.GET), handler);
		}

		public BaseRoute (String identifier, String descriptor, EnumSet<Method> methods, T handler) {
			if ( ! descriptor.startsWith ("/") ) {
				throw new RuntimeException ("invalid descriptor: " + descriptor);
			}

			this.identifier = identifier;
			this.handler = handler;
			this.methods = methods;
			this.descriptor = descriptor.substring (1).split ("/");
			this.shouldMatch = new boolean [this.descriptor.length];
			this.allowTail = this.descriptor[this.descriptor.length - 1].startsWith ("::");

			processDescriptor ();
		}

		@Override public List<String> getIdentifiers () {
			return Collections.singletonList (identifier);
		}

		@Override public T getHandler (String identifier) {
			return handler;
		}

		@Override public String linkTo (String id, Map<String, String> params) {
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

	public static class RootRoute<T> extends BaseRoute<T> {
		public RootRoute (T handler) {
			super ("root", "ROOT", handler);
		}

		@Override public String toId (Method method, String url, String [] parts) {
			return parts.length == 1 && "".equals (parts[1]) ? identifier : null;
		}

		@Override public Bag<String, String> extractMatches (String identifier, String url, String [] parts) {
			return new Bag<> ();
		}
	}

	public static class ControllerRoute extends SimpleRoute<ControllerFunction> {
		public ControllerRoute (String identifier, String descriptor, ControllerFunction handler) {
			super (identifier, descriptor, handler);
		}

		public ControllerRoute (String identifier, String descriptor, EnumSet<Method> methods, ControllerFunction handler) {
			super (identifier, descriptor, methods, handler);
		}
	}

	public static class SocketRoute extends SimpleRoute<SocketFunction> {
		public SocketRoute (String identifier, String descriptor, SocketFunction handler) {
			super (identifier, descriptor, handler);
		}
	}

	/**
	 * This will work for url that only have exact matching parts or a variable part.
	 * Things such as `/fixed/part/:variable/or/:other/variables`.
	 *
	 * basic route matching, should be very fast, if the split string is passed instead
	 * of the plain url this should not have a noticeable impact on performance.
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
	public static class SimpleRoute<T> extends BaseRoute<T> {
		public SimpleRoute (String identifier, String descriptor, T handler) {
			super (identifier, descriptor, handler);
		}

		public SimpleRoute (String identifier, String descriptor, EnumSet<Method> methods, T handler) {
			super (identifier, descriptor, methods, handler);
		}

		@Override public String toId (Method method, String url, String [] parts) {
			if ( ! methods.contains (method) ) { return null; }
			if ( ! this.allowTail && parts.length != this.descriptor.length ) { return null; }

			for ( int i = 0; i < descriptor.length; i++ ) {
				if ( shouldMatch[i] && ! descriptor[i].equals (parts[i]) ) {
					return null;
				}
			}

			return identifier;
		}

		@Override public Bag<String, String> extractMatches (String identifier, String url, String [] parts) {
			Bag<String, String> extracted = new Bag<> ();
			int last = descriptor.length - 1;
			for ( int i = 0; i < descriptor.length; i++ ) {
				if ( shouldMatch [i] ) { continue; }
				if ( i == last && descriptor[i].startsWith ("::") ) {
					extracted.add (descriptor[i].substring (2), join (parts, i));
				} else {
					extracted.add (descriptor[i].substring (1), parts[i]);
				}
			}
			return extracted;
		}

		private String join (String [] parts, int index) {
			StringBuilder builder = new StringBuilder ();
			for ( int i = index; i < parts.length; i++ ) {
				builder.append ("/").append (parts[i]);
			}
			return builder.toString ();
		}
	}

	abstract public List<String> getIdentifiers ();
	abstract public String toId (Method method, String url, String [] parts);
	abstract public Bag<String, String> extractMatches (String identifier, String url, String [] parts);
	abstract public T getHandler (String identifier);
	abstract public String linkTo (String identifier, Map<String, String> params);

}
