package org.tutske.rest;

import org.tutske.rest.data.RestStructure;


@FunctionalInterface
public interface ControllerFunction extends ThrowingFunction<HttpRequest, RestStructure> {

	RestStructure apply (HttpRequest request) throws Exception;

}
