package org.tutske.rest.routes;

import org.tutske.rest.HttpRequest;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;


abstract class BaseRoute<T> implements UrlRoute<T> {
	protected final String identifier;
	protected final T handler;
	protected final EnumSet<HttpRequest.Method> methods;
	protected final String [] descriptor;
	protected final boolean [] shouldMatch;
	protected final boolean allowTail;

	public BaseRoute (String identifier, String descriptor, T handler) {
		this (identifier, descriptor, EnumSet.of (HttpRequest.Method.GET), handler);
	}

	public BaseRoute (String identifier, String descriptor, EnumSet<HttpRequest.Method> methods, T handler) {
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

	@Override
	public List<String> getIdentifiers () {
		return Collections.singletonList (identifier);
	}

	@Override
	public T getHandler (String identifier) {
		return handler;
	}

	@Override
	public String linkTo (String id, Map<String, String> params) {
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
