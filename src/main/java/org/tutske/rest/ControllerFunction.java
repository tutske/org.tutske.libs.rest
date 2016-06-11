package org.tutske.rest;

import org.tutske.rest.data.RestObject;


@FunctionalInterface
public interface ControllerFunction extends ThrowingFunction<HttpRequest, RestObject> {

	RestObject apply (HttpRequest request) throws Exception;

}
