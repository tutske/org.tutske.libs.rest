package org.tutske.websocket;


import org.tutske.rest.objects.RestObject;


@FunctionalInterface
public interface ControllerFunction {

	RestObject apply (HttpRequest request) throws Exception;

}
