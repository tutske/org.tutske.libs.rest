package org.tutske.websocket;


@FunctionalInterface
public interface ControllerFunction {

	RestObject apply (HttpRequest request) throws Exception;

}
