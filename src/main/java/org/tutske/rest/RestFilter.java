package org.tutske.rest;

import org.tutske.rest.ControllerFunction;
import org.tutske.rest.Filter;
import org.tutske.rest.HttpRequest;


public interface RestFilter extends Filter<HttpRequest, ControllerFunction> {
}
