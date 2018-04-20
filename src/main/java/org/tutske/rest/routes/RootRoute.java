package org.tutske.rest.routes;

import org.tutske.rest.HttpRequest;
import org.tutske.utils.Bag;


public class RootRoute<T> extends BaseRoute<T> {
	public RootRoute (T handler) {
		super ("root", "/", handler);
	}

	@Override public String toId (HttpRequest.Method method, String url, String [] parts) {
		return parts.length == 1 && "".equals (parts[1]) ? identifier : null;
	}

	@Override public Bag<String, String> extractMatches (String identifier, String url, String [] parts) {
		return new Bag<> ();
	}
}
