package org.tutske.rest.routes;

import org.tutske.rest.ControllerFunction;
import org.tutske.rest.HttpRequest;

import java.util.EnumSet;


public class ControllerRoute extends SimpleRoute<ControllerFunction> {

	public ControllerRoute (String identifier, String descriptor, ControllerFunction handler) {
		super (identifier, descriptor, handler);
	}

	public ControllerRoute (String identifier, String descriptor, EnumSet<HttpRequest.Method> methods, ControllerFunction handler) {
		super (identifier, descriptor, methods, handler);
	}

}
