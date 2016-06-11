package org.tutske.rest.internals;

import org.tutske.rest.ControllerFunction;
import org.tutske.rest.HttpRequest;


public interface RestFilter extends Filter<HttpRequest, ControllerFunction> {
}
