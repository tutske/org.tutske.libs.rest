package org.tutske.rest.routes;

import org.tutske.rest.SocketFunction;


public class SocketRoute extends SimpleRoute<SocketFunction> {

	public SocketRoute (String identifier, String descriptor, SocketFunction handler) {
		super (identifier, descriptor, handler);
	}

}
