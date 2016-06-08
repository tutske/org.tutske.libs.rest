package org.tutske.rest;

import org.tutske.rest.data.RestObject;


@FunctionalInterface
public interface ControllerFunction {

	RestObject apply (HttpRequest request) throws Exception;

}
