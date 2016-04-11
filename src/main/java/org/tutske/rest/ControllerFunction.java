package org.tutske.rest;


@FunctionalInterface
public interface ControllerFunction {

	RestObject apply (HttpRequest request) throws Exception;

}
