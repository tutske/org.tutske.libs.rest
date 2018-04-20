package org.tutske.rest.routes;

import org.tutske.rest.HttpRequest;
import org.tutske.utils.Bag;

import java.util.EnumSet;


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
public class SimpleRoute<T> extends BaseRoute<T> {

	public SimpleRoute (String identifier, String descriptor, T handler) {
		super (identifier, descriptor, handler);
	}

	public SimpleRoute (String identifier, String descriptor, EnumSet<HttpRequest.Method> methods, T handler) {
		super (identifier, descriptor, methods, handler);
	}

	@Override
	public String toId (HttpRequest.Method method, String url, String [] parts) {
		if ( ! methods.contains (method) ) { return null; }
		if ( ! this.allowTail && parts.length != this.descriptor.length ) { return null; }

		for ( int i = 0; i < descriptor.length; i++ ) {
			if ( shouldMatch[i] && ! descriptor[i].equals (parts[i]) ) {
				return null;
			}
		}

		return identifier;
	}

	@Override
	public Bag<String, String> extractMatches (String identifier, String url, String [] parts) {
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
