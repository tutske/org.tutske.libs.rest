package org.tutske.rest.routes;

import org.tutske.rest.HttpRequest;
import org.tutske.utils.Bag;

import java.util.*;


public class GroupedRoute<T> extends BaseRoute<T> {

	private final List<UrlRoute<? extends T>> subs = new LinkedList<> ();
	private final Map<String, UrlRoute<? extends T>> mapped = new LinkedHashMap<> ();
	private final boolean isRoot;

	public GroupedRoute (String descriptor, UrlRoute<T> ... subs) {
		this (descriptor, EnumSet.noneOf (HttpRequest.Method.class), Arrays.asList (subs));
	}

	public GroupedRoute (String descriptor, List<UrlRoute<T>> subs) {
		this (descriptor, EnumSet.noneOf (HttpRequest.Method.class), subs);
	}

	public GroupedRoute (String descriptor, EnumSet<HttpRequest.Method> methods, UrlRoute<T> ... subs) {
		this (descriptor, methods, Arrays.asList (subs));
	}

	public GroupedRoute (String descriptor, EnumSet<HttpRequest.Method> methods, List<UrlRoute<T>> subs) {
		super ("", descriptor, methods, null);

		if ( allowTail ) {
			throw new RuntimeException ("descriptor of grouped route can not allow a tail");
		}

		this.isRoot = this.descriptor.length == 1 && this.descriptor[0].isEmpty ();
		this.addSubs (subs);
	}

	@Override public List<String> getIdentifiers () {
		return new LinkedList<> (mapped.keySet ());
	}

	@Override
	public String toId (HttpRequest.Method method, String url, String [] parts) {
		if ( isRoot ) { return toSubId (method, url, parts); }

		if ( parts.length < descriptor.length ) { return null; }
		if ( ! methods.isEmpty () && ! methods.contains (method) ) { return null; }

		for ( int i = 0; i < descriptor.length; i++ ) {
			if ( shouldMatch[i] && ! descriptor[i].equals (parts[i]) ) {
				return null;
			}
		}

		return toSubId (method, url, tail (parts));
	}

	@Override
	public Bag<String, String> extractMatches (String identifier, String url, String [] parts) {
		if ( isRoot ) {
			return getRoute (identifier).extractMatches (identifier, url, parts);
		}

		UrlRoute<? extends T> route = getRoute (identifier);
		Bag<String, String> extracted = new Bag<> ();

		for ( int i = 0; i < descriptor.length; i++ ) {
			if ( ! shouldMatch [i] ) {
				extracted.add (descriptor[i].substring (1), parts[i]);
			}
		}

		String [] tail = tail (parts);
		extracted.addAll (route.extractMatches (identifier, url, tail));

		return extracted;
	}

	@Override
	public T getHandler (String identifier) {
		return getRoute (identifier).getHandler (identifier);
	}

	@Override
	public String linkTo (String identifier, Map<String, String> params) {
		UrlRoute<? extends T> route = getRoute (identifier);
		return "/" + super.linkTo (identifier, params) + route.linkTo (identifier, params);
	}

	private String toSubId (HttpRequest.Method method, String url, String [] parts) {
		for ( UrlRoute<? extends T> route : subs ) {
			String id = route.toId (method, url, parts);
			if ( id != null ) { return id; }
		}
		return null;
	}

	private void addSubs (List<UrlRoute<T>> subs) {
		String msg = "Grouped router already has a sub route with that name: ";
		subs.forEach (sub -> sub.getIdentifiers ().forEach (id -> {
			if ( this.mapped.containsKey (id) ) { throw new RuntimeException (msg + id); }
			this.mapped.put (id, sub);
		}));
		this.subs.addAll (subs);
	}

	private UrlRoute<? extends T> getRoute (String identifier) {
		UrlRoute<? extends T> route = mapped.get (identifier);
		if ( route != null ) { return route; }
		throw new RuntimeException ("No such route in group: " + identifier);
	}

	private String [] tail (String [] parts) {
		if ( parts.length == descriptor.length ) { return new String [] { "" }; }
		String [] tail = new String [parts.length - descriptor.length];
		System.arraycopy (parts, descriptor.length, tail, 0, tail.length);
		return tail;
	}

}
