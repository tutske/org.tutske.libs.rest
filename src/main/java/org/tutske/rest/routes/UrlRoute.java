package org.tutske.rest.routes;

import org.tutske.utils.Bag;

import static org.tutske.rest.HttpRequest.Method;

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
public interface UrlRoute<T> {

	List<String> getIdentifiers ();
	String toId (Method method, String url, String [] parts);
	Bag<String, String> extractMatches (String identifier, String url, String [] parts);
	T getHandler (String identifier);
	String linkTo (String identifier, Map<String, String> params);

}
